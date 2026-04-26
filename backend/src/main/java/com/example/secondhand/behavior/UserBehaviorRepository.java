package com.example.secondhand.behavior;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {

    boolean existsByUser_IdAndItem_IdAndTypeAndCreatedAtAfter(
            Long userId,
            Long itemId,
            String type,
            Instant after
    );

    // ✅ 取用户最近浏览过的 itemId（按 createdAt 倒序）
    @Query("select b.item.id from UserBehavior b where b.user.id = :userId and b.type = :type order by b.createdAt desc")
    List<Long> findRecentItemIds(Long userId, String type, Pageable pageable);
}