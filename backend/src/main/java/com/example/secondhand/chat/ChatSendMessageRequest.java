package com.example.secondhand.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 新增：发送聊天消息请求体
public record ChatSendMessageRequest(
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 500, message = "消息内容不能超过 500 字")
        String content
) {
    public String safeContent() {
        return content == null ? "" : content.trim();
    }
}