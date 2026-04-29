package com.example.secondhand.ai;

// 新增文件：AI 多轮对话消息对象，role 只能使用 user / assistant / system
public record AiMessage(
        String role,
        String content
) {
    public boolean valid() {
        if (role == null || content == null || content.isBlank()) return false;
        return "user".equals(role) || "assistant".equals(role) || "system".equals(role);
    }
}
