package com.example.secondhand.chat;

import java.time.Instant;

// 新增：聊天会话返回对象
public record ChatConversationResponse(
        Long id,
        Long itemId,
        String itemTitle,
        String itemCoverImageUrl,
        Long buyerId,
        String buyerUsername,
        String buyerNickname,
        Long sellerId,
        String sellerUsername,
        String sellerNickname,
        Long otherUserId,
        String otherUsername,
        String otherNickname,
        String lastMessage,
        Instant lastMessageAt,
        Long unreadCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static ChatConversationResponse from(
            ChatConversation conversation,
            Long currentUserId,
            ChatMessage lastMessage,
            Long unreadCount
    ) {
        boolean currentIsBuyer = conversation.getBuyer().getId().equals(currentUserId);

        Long otherUserId = currentIsBuyer
                ? conversation.getSeller().getId()
                : conversation.getBuyer().getId();

        String otherUsername = currentIsBuyer
                ? conversation.getSeller().getUsername()
                : conversation.getBuyer().getUsername();

        String otherNickname = currentIsBuyer
                ? conversation.getSeller().getNickname()
                : conversation.getBuyer().getNickname();

        return new ChatConversationResponse(
                conversation.getId(),
                conversation.getItem().getId(),
                conversation.getItem().getTitle(),
                conversation.getItem().getCoverImageUrl(),
                conversation.getBuyer().getId(),
                conversation.getBuyer().getUsername(),
                conversation.getBuyer().getNickname(),
                conversation.getSeller().getId(),
                conversation.getSeller().getUsername(),
                conversation.getSeller().getNickname(),
                otherUserId,
                otherUsername,
                otherNickname,
                lastMessage == null ? "" : lastMessage.getContent(),
                lastMessage == null ? null : lastMessage.getCreatedAt(),
                unreadCount == null ? 0L : unreadCount,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}