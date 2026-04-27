package com.example.secondhand.admin;

import com.example.secondhand.order.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record AdminUpdateOrderStatusRequest(
        @NotNull OrderStatus status,
        String reason
) {
    public String safeReason() {
        if (reason == null || reason.isBlank()) {
            return "未填写";
        }
        return reason.trim();
    }
}