package com.example.secondhand.admin;

public record AdminReasonRequest(String reason) {

    public String safeReason() {
        if (reason == null || reason.isBlank()) {
            return "未填写";
        }
        return reason.trim();
    }
}