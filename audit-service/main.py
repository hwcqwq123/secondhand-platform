# 修改后完整文件：audit-service/main.py
# 功能：本地违禁词库 + 本地正则规则 + 国产开源大模型本地部署审核
# 默认模型接口采用 Ollama OpenAI-compatible API：
#   ollama pull qwen3:4b
#   ollama serve
#   AUDIT_USE_LLM=true python main.py
# 也可以把 AUDIT_LLM_MODEL 改成 deepseek-r1:7b 等本地开源模型。

import os
import re
import json
from pathlib import Path
from typing import List, Optional, Dict, Any

import requests
from fastapi import FastAPI
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field


class UTF8JSONResponse(JSONResponse):
    media_type = "application/json; charset=utf-8"


APP_NAME = "secondhand-audit-service"

# 修改：默认仍然允许不开模型启动；需要真正启用本地开源模型时设置 AUDIT_USE_LLM=true
USE_LLM = os.getenv("AUDIT_USE_LLM", "false").lower() == "true"

# 修改：默认给出本地开源模型 OpenAI-compatible 地址
# Ollama 默认地址：http://127.0.0.1:11434/v1
# DeepSeek 官方在线 API：https://api.deepseek.com
# 通义千问 / 阿里云百炼兼容模式：中国区 https://dashscope.aliyuncs.com/compatible-mode/v1
LLM_BASE_URL = os.getenv("AUDIT_LLM_BASE_URL", "http://127.0.0.1:11434/v1").strip()

# 修改：Ollama 本地部署通常不校验 API Key；在线 API 必须填真实 Key
LLM_API_KEY = os.getenv("AUDIT_LLM_API_KEY", "").strip()

# 修改：默认国产开源模型 qwen3:4b，可改成 deepseek-r1:7b
LLM_MODEL = os.getenv("AUDIT_LLM_MODEL", "qwen3:1.7b").strip()

MODEL_REQUIRED = os.getenv("AUDIT_MODEL_REQUIRED", "false").lower() == "true"
LLM_TIMEOUT = int(os.getenv("AUDIT_LLM_TIMEOUT", "120"))
MAX_TEXT_LENGTH = int(os.getenv("AUDIT_MAX_TEXT_LENGTH", "600"))

BASE_DIR = Path(__file__).resolve().parent
BANNED_WORDS_FILE = BASE_DIR / "banned_words.txt"

app = FastAPI(title=APP_NAME, version="2.0.0", default_response_class=UTF8JSONResponse)


class AuditRequest(BaseModel):
    text: str = Field(default="", description="需要审核的文本")
    scene: str = Field(default="ITEM", description="审核场景，例如 ITEM 或 CHAT")


class AuditResponse(BaseModel):
    status: str
    allowed: bool
    score: float
    label: str
    reason: str
    matchedWords: List[str]


def load_banned_words() -> List[str]:
    if not BANNED_WORDS_FILE.exists():
        return []
    words = []
    for line in BANNED_WORDS_FILE.read_text(encoding="utf-8").splitlines():
        word = line.strip()
        if word and not word.startswith("#"):
            words.append(word)
    return words


BANNED_WORDS = load_banned_words()


def normalize_text(text: str) -> str:
    return (text or "").strip()


def find_banned_words(text: str) -> List[str]:
    lower_text = text.lower()
    return [word for word in BANNED_WORDS if word and word.lower() in lower_text]


def keyword_audit(text: str) -> Optional[AuditResponse]:
    matched = find_banned_words(text)
    if matched:
        return AuditResponse(
            status="BLOCK",
            allowed=False,
            score=1.0,
            label="关键词命中",
            reason="内容包含违禁词：" + "、".join(matched),
            matchedWords=matched,
        )
    return None


def rule_audit(text: str) -> Optional[AuditResponse]:
    # 修改：本地规则审核，不依赖任何模型
    risk_patterns = [
        {
            "label": "引流联系方式内容",
            "status": "REVIEW",
            "score": 0.65,
            "pattern": r"(微信|vx|v信|qq|QQ|加我|私聊|联系我|手机号|电话|电联|线下加|私下交易)",
        },
        {
            "label": "诈骗广告内容",
            "status": "REVIEW",
            "score": 0.70,
            "pattern": r"(刷单|返利|兼职日结|稳赚|高回报|套现|代付|跑分|资金盘)",
        },
        {
            "label": "违禁交易内容",
            "status": "BLOCK",
            "score": 0.90,
            "pattern": r"(假币|枪支|枪|弹药|毒品|迷药|银行卡四件套|办证|代考|管制刀具)",
        },
        {
            "label": "色情低俗内容",
            "status": "BLOCK",
            "score": 0.88,
            "pattern": r"(裸聊|约炮|色情|成人视频|低俗服务)",
        },
        {
            "label": "辱骂攻击内容",
            "status": "REVIEW",
            "score": 0.60,
            "pattern": r"(傻逼|垃圾人|死全家|滚蛋)",
        },
    ]

    for item in risk_patterns:
        if re.search(item["pattern"], text, re.IGNORECASE):
            return AuditResponse(
                status=item["status"],
                allowed=item["status"] == "PASS",
                score=item["score"],
                label=item["label"],
                reason=f"本地规则判断内容疑似属于：{item['label']}",
                matchedWords=[],
            )
    return None


def build_audit_prompt(text: str, scene: str) -> str:
    scene_name = "商品发布" if scene == "ITEM" else "聊天消息"
    return f"""
你是校园二手交易平台的内容审核系统。请审核下面这段{scene_name}内容是否存在风险。

审核类别包括：
1. 正常二手交易内容
2. 违禁交易内容，例如枪支、弹药、毒品、假币、迷药、非法证件等
3. 诈骗广告内容，例如刷单、返利、跑分、套现、高回报兼职等
4. 辱骂攻击内容
5. 色情低俗内容
6. 引流联系方式内容，例如诱导加微信、QQ、手机号、私下交易等

请严格只返回 JSON，不要返回解释文字，不要使用 Markdown。
返回格式：
{{
  "status": "PASS 或 REVIEW 或 BLOCK",
  "allowed": true 或 false,
  "score": 0 到 1 之间的小数,
  "label": "审核类别",
  "reason": "简短原因"
}}

判断规则：
- 明确违法违规交易：BLOCK
- 疑似诈骗、引流、辱骂、低俗：REVIEW
- 普通二手商品描述：PASS

待审核内容：
{text}
""".strip()


def extract_json_from_text(content: str) -> Dict[str, Any]:
    content = (content or "").strip()
    content = re.sub(r"(?s)<think>.*?</think>", "", content).strip()
    try:
        return json.loads(content)
    except Exception:
        pass

    match = re.search(r"\{.*\}", content, re.DOTALL)
    if not match:
        raise ValueError("模型返回内容不是 JSON：" + content[:200])
    return json.loads(match.group(0))


def is_local_ollama_url(url: str) -> bool:
    u = (url or "").lower()
    return "127.0.0.1" in u or "localhost" in u or "host.docker.internal" in u


def call_openai_compatible_llm(text: str, scene: str) -> AuditResponse:
    if not USE_LLM:
        return AuditResponse(
            status="PASS",
            allowed=True,
            score=0.0,
            label="本地审核通过",
            reason="大模型审核未启用，已使用本地违禁词库和本地规则审核通过",
            matchedWords=[],
        )

    if not LLM_BASE_URL:
        return handle_llm_error("未配置 AUDIT_LLM_BASE_URL")
    if not LLM_MODEL:
        return handle_llm_error("未配置 AUDIT_LLM_MODEL")

    base_url = LLM_BASE_URL.rstrip("/")
    url = base_url if base_url.endswith("/chat/completions") else f"{base_url}/chat/completions"

    payload = {
        "model": LLM_MODEL,
        "messages": [
            {"role": "system", "content": "你是严格的内容安全审核模型，只能返回 JSON。"},
            {"role": "user", "content": build_audit_prompt(text, scene)},
        ],
        "temperature": 0,
        "stream": False,
    }

    # 修改：本地 Ollama 没有 key 也可以；在线 DeepSeek / 千问必须填 key
    final_key = LLM_API_KEY or ("ollama" if is_local_ollama_url(LLM_BASE_URL) else "")
    if not final_key:
        return handle_llm_error("未配置 AUDIT_LLM_API_KEY。在线模型必须配置 API Key，本地 Ollama 可不填。")

    headers = {
        "Authorization": f"Bearer {final_key}",
        "Content-Type": "application/json; charset=utf-8",
    }

    try:
        response = requests.post(url, headers=headers, json=payload, timeout=LLM_TIMEOUT)
        response.raise_for_status()
        data = response.json()
        content = data.get("choices", [{}])[0].get("message", {}).get("content", "")
        result = extract_json_from_text(content)

        status = str(result.get("status", "REVIEW")).upper()
        if status not in ["PASS", "REVIEW", "BLOCK"]:
            status = "REVIEW"

        allowed = bool(result.get("allowed", status == "PASS"))
        if status != "PASS":
            allowed = False

        return AuditResponse(
            status=status,
            allowed=allowed,
            score=float(result.get("score", 0.5)),
            label=str(result.get("label", "大模型审核")),
            reason=str(result.get("reason", "大模型审核完成")),
            matchedWords=[],
        )
    except Exception as e:
        return handle_llm_error("大模型审核调用失败：" + str(e))


def handle_llm_error(message: str) -> AuditResponse:
    if MODEL_REQUIRED:
        return AuditResponse(
            status="REVIEW",
            allowed=False,
            score=0.0,
            label="大模型审核异常",
            reason=message,
            matchedWords=[],
        )

    return AuditResponse(
        status="PASS",
        allowed=True,
        score=0.0,
        label="本地审核通过",
        reason=message + "；当前 MODEL_REQUIRED=false，本地开发模式已放行",
        matchedWords=[],
    )


@app.get("/")
def root():
    return health()


@app.get("/health")
def health():
    return {
        "name": APP_NAME,
        "status": "UP",
        "useLLM": USE_LLM,
        "llmBaseUrl": LLM_BASE_URL,
        "llmModel": LLM_MODEL,
        "modelRequired": MODEL_REQUIRED,
        "bannedWordsCount": len(BANNED_WORDS),
    }


@app.post("/audit", response_model=AuditResponse)
def audit(req: AuditRequest):
    text = normalize_text(req.text)

    if not text:
        return AuditResponse(status="PASS", allowed=True, score=0.0, label="空文本", reason="空文本无需审核", matchedWords=[])

    if len(text) > MAX_TEXT_LENGTH:
        text = text[:MAX_TEXT_LENGTH]

    keyword_result = keyword_audit(text)
    if keyword_result is not None:
        return keyword_result

    rule_result = rule_audit(text)
    if rule_result is not None:
        return rule_result

    return call_openai_compatible_llm(text, req.scene)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="127.0.0.1", port=8001, reload=False)
