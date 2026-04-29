package com.example.secondhand.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// 新增：聊天消息 Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 新增：按时间顺序查询某个会话的全部消息
    List<ChatMessage> findByConversation_IdOrderByCreatedAtAsc(Long conversationId);

    // 新增：查询某个会话的最后一条消息
    Optional<ChatMessage> findTopByConversation_IdOrderByCreatedAtDesc(Long conversationId);

    // 新增：统计当前用户未读消息数量
    long countByConversation_IdAndSender_IdNotAndReadFlagFalse(Long conversationId, Long senderId);

    // 新增：查询当前用户未读消息列表
    List<ChatMessage> findByConversation_IdAndSender_IdNotAndReadFlagFalse(Long conversationId, Long senderId);
}