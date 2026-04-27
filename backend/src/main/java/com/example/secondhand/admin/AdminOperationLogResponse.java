package com.example.secondhand.admin;

import java.time.Instant;

public record AdminOperationLogResponse(
        Long id,
        String adminUsername,
        String action,
        String targetType,
        Long targetId,
        String detail,
        String reason,
        Instant createdAt
) {
    public static AdminOperationLogResponse from(AdminOperationLog log) {
        return new AdminOperationLogResponse(
                log.getId(),
                log.getAdminUsername(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetail(),
                log.getReason(),
                log.getCreatedAt()
        );
    }
}