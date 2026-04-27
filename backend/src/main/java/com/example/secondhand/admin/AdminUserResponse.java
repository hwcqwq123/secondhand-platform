package com.example.secondhand.admin;

import com.example.secondhand.user.User;
import com.example.secondhand.user.UserStatus;

import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String role,
        UserStatus status,
        Instant createdAt,
        Long itemCount,
        Long buyOrderCount,
        Long sellOrderCount
) {
    public static AdminUserResponse from(
            User user,
            long itemCount,
            long buyOrderCount,
            long sellOrderCount
    ) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                itemCount,
                buyOrderCount,
                sellOrderCount
        );
    }
}