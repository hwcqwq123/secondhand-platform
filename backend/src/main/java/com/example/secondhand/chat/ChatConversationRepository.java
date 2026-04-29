package com.example.secondhand.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 新增：聊天会话 Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByItem_IdAndBuyer_IdAndSeller_Id(
            Long itemId,
            Long buyerId,
            Long sellerId
    );

    // 新增：查询当前用户参与的所有会话
    @Query("""
            select c from ChatConversation c
            join fetch c.item i
            join fetch c.buyer b
            join fetch c.seller s
            where c.buyer.id = :userId or c.seller.id = :userId
            order by c.updatedAt desc
            """)
    List<ChatConversation> findMyConversations(@Param("userId") Long userId);

    // 新增：查询会话详情，并一次性加载商品、买家、卖家
    @Query("""
            select c from ChatConversation c
            join fetch c.item i
            join fetch c.buyer b
            join fetch c.seller s
            where c.id = :id
            """)
    Optional<ChatConversation> findByIdWithAll(@Param("id") Long id);
}