package com.example.secondhand.ai;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 新增文件：AI 客服请求体
public record AiChatRequest(
        @NotBlank(message = "问题不能为空")
        @Size(max = 500, message = "问题不能超过 500 字")
        String message,

        // 新增：前端可传最近几轮对话，实现简单多轮上下文
        List<AiMessage> history
) {
    public String safeMessage() {
        return message == null ? "" : message.trim();
    }
}
