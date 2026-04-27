package com.example.secondhand.admin;

import com.example.secondhand.user.UserStatus;

public record AdminUpdateUserRequest(
        String role,
        UserStatus status,
        String reason
) {
    public String safeReason() {
        if (reason == null || reason.isBlank()) {
            return "未填写";
        }
        return reason.trim();
    }
}