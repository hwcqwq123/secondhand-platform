package com.example.secondhand.chat;

import java.time.Instant;

// 新增：聊天消息返回对象
public record ChatMessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderUsername,
        String senderNickname,
        String content,
        String auditStatus,
        String auditReason,
        Double auditScore,
        Boolean readFlag,
        Instant createdAt,
        Boolean mine
) {
    public static ChatMessageResponse from(ChatMessage message, Long currentUserId) {
        boolean mine = message.getSender().getId().equals(currentUserId);

        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getAuditStatus(),
                message.getAuditReason(),
                message.getAuditScore(),
                message.getReadFlag(),
                message.getCreatedAt(),
                mine
        );
    }
}