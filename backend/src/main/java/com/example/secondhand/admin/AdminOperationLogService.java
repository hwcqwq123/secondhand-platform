package com.example.secondhand.admin;

import org.springframework.stereotype.Service;

@Service
public class AdminOperationLogService {

    private final AdminOperationLogRepository logs;

    public AdminOperationLogService(AdminOperationLogRepository logs) {
        this.logs = logs;
    }

    public void record(
            String adminUsername,
            String action,
            String targetType,
            Long targetId,
            String detail,
            String reason
    ) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminUsername(adminUsername == null ? "unknown" : adminUsername);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setReason(reason == null || reason.isBlank() ? "未填写" : reason.trim());
        logs.save(log);
    }
}