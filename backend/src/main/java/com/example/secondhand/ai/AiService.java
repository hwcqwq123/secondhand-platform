package com.example.secondhand.ai;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.secondhand.item.Item;
import com.example.secondhand.item.ItemRepository;
import com.example.secondhand.item.ItemResponse;
import com.example.secondhand.item.ItemStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// 新增文件：AI 客服服务
// 功能：
// 1. 支持本地开源模型：Ollama OpenAI-compatible API，例如 qwen3:4b、deepseek-r1:7b
// 2. 支持在线国产模型 API：DeepSeek、阿里云百炼/通义千问 DashScope compatible-mode
// 3. LLM 失败时自动降级为本地规则客服 + 商品推荐
@Service
public class AiService {

    private final ItemRepository items;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${app.ai.provider:ollama-local}")
    private String provider;

    @Value("${app.ai.base-url:http://127.0.0.1:11434/v1}")
    private String baseUrl;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:qwen3:4b}")
    private String model;

    @Value("${app.ai.timeout-ms:30000}")
    private int timeoutMs;

    @Value("${app.ai.max-suggestions:6}")
    private int maxSuggestions;

    public AiService(ItemRepository items, ObjectMapper objectMapper) {
        this.items = items;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        this.restTemplate = new RestTemplate(factory);
    }

    public AiChatResponse chat(AiChatRequest req) {
        String question = req.safeMessage();
        List<ItemResponse> suggestions = findSuggestions(question, maxSuggestions);

        if (!aiEnabled) {
            return new AiChatResponse(
                    buildFallbackAnswer(question, suggestions, "AI 大模型未启用"),
                    suggestions,
                    provider,
                    model,
                    false,
                    true
            );
        }

        try {
            String answer = callOpenAiCompatibleModel(question, req.history(), suggestions);
            return new AiChatResponse(
                    answer,
                    suggestions,
                    provider,
                    model,
                    true,
                    false
            );
        } catch (Exception e) {
            return new AiChatResponse(
                    buildFallbackAnswer(question, suggestions, "大模型调用失败，已使用本地规则降级回答：" + e.getMessage()),
                    suggestions,
                    provider,
                    model,
                    true,
                    true
            );
        }
    }

    private String callOpenAiCompatibleModel(
            String question,
            List<AiMessage> history,
            List<ItemResponse> suggestions
    ) throws Exception {
        String finalBaseUrl = stripRightSlash(baseUrl);
        if (finalBaseUrl.isBlank()) {
            throw new IllegalStateException("未配置 app.ai.base-url");
        }

        String url = finalBaseUrl.endsWith("/chat/completions")
                ? finalBaseUrl
                : finalBaseUrl + "/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", buildSystemPrompt(suggestions)
        ));

        if (history != null) {
            history.stream()
                    .filter(m -> m != null && m.valid())
                    .limit(10)
                    .forEach(m -> messages.add(Map.of(
                            "role", m.role(),
                            "content", trim(m.content(), 800)
                    )));
        }

        messages.add(Map.of("role", "user", "content", question));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("temperature", 0.3);
        payload.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 修改：Ollama 本地 OpenAI-compatible 接口通常不校验 key；这里没有配置时给一个占位值，避免部分兼容 SDK 报错
        String finalKey = apiKey == null || apiKey.isBlank() ? "ollama" : apiKey.trim();
        headers.setBearerAuth(finalKey);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, headers),
                String.class
        );

        String body = response.getBody();
        if (body == null || body.isBlank()) {
            throw new IllegalStateException("模型返回为空");
        }

        JsonNode root = objectMapper.readTree(body);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        String content = contentNode.asText("").trim();

        if (content.isBlank()) {
            throw new IllegalStateException("模型没有返回 message.content");
        }

        return removeThinkTag(content);
    }

    private String buildSystemPrompt(List<ItemResponse> suggestions) {
        String itemContext = suggestions.isEmpty()
                ? "当前没有匹配到商品。"
                : suggestions.stream()
                .map(i -> String.format(
                        Locale.ROOT,
                        "ID=%d，标题=%s，价格=%s，分类=%s，状态=%s",
                        i.id(),
                        safe(i.title()),
                        i.price(),
                        safe(i.board()),
                        i.status()
                ))
                .collect(Collectors.joining("\n"));

        return """
                你是校园二手交易平台的 AI 客服。请使用简洁中文回答用户问题。
                你的职责：
                1. 帮用户找商品、推荐商品、解释交易流程。
                2. 不要编造不存在的订单、支付状态、管理员操作结果。
                3. 如果用户问商品，优先结合下面的候选商品回答，并提醒用户点击商品卡片查看详情。
                4. 如果用户要求违规交易、私下引流、诈骗、色情低俗、枪支毒品等内容，必须拒绝。
                5. 回答控制在 120 字以内。

                当前候选商品：
                %s
                """.formatted(itemContext);
    }

    private List<ItemResponse> findSuggestions(String question, int limit) {
        String keyword = extractKeyword(question);
        List<Item> pool;

        if (!keyword.isBlank()) {
            pool = items.searchWithSeller(keyword);
        } else {
            pool = items.findAllWithSeller();
        }

        return pool.stream()
                .filter(this::visible)
                .limit(Math.max(1, limit))
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }

    private boolean visible(Item item) {
        return item != null
                && !Boolean.TRUE.equals(item.getDeleted())
                && item.getStatus() != ItemStatus.SOLD
                && item.getStatus() != ItemStatus.OFF_SHELF;
    }

    private ItemResponse toItemResponse(Item i) {
        return new ItemResponse(
                i.getId(),
                i.getTitle(),
                i.getDescription(),
                i.getPrice(),
                i.getStatus(),
                i.getCreatedAt(),
                new ItemResponse.SellerResponse(
                        i.getSeller().getId(),
                        i.getSeller().getUsername(),
                        i.getSeller().getNickname()
                ),
                i.getCoverImageUrl(),
                List.of(),
                i.getBoard()
        );
    }

    private String buildFallbackAnswer(String question, List<ItemResponse> suggestions, String reason) {
        if (suggestions.isEmpty()) {
            return reason + "。我暂时没有找到相关商品，你可以换个关键词，例如：耳机、教材、自行车、鼠标。";
        }

        String names = suggestions.stream()
                .limit(3)
                .map(i -> "《" + safe(i.title()) + "》")
                .collect(Collectors.joining("、"));

        return reason + "。根据你的问题“" + trim(question, 40) + "”，我为你找到了 " + names + " 等商品，可以点击下方商品卡片查看详情。";
    }

    private String extractKeyword(String question) {
        String text = safe(question)
                .replace("推荐", " ")
                .replace("想买", " ")
                .replace("我要", " ")
                .replace("有没有", " ")
                .replace("帮我找", " ")
                .replace("二手", " ")
                .replace("商品", " ")
                .replace("多少钱", " ")
                .trim();

        text = text.replaceAll("[，。！？、,.!?;；:：]", " ").trim();
        if (text.length() > 20) {
            text = text.substring(0, 20);
        }
        return text;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String trim(String s, int max) {
        String t = safe(s);
        return t.length() <= max ? t : t.substring(0, max);
    }

    private static String stripRightSlash(String s) {
        String t = safe(s);
        while (t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }

    private static String removeThinkTag(String content) {
        return content.replaceAll("(?s)<think>.*?</think>", "").trim();
    }
}
