package com.example.secondhand.audit;

import java.util.List;

// 新增文件：后端内部使用的审核结果对象
public record AuditDecision(
        boolean allowed,
        String status,
        double score,
        String label,
        String reason,
        List<String> matchedWords
) {
    public static AuditDecision pass(String reason) {
        return new AuditDecision(
                true,
                "PASS",
                0.0,
                "PASS",
                reason,
                List.of()
        );
    }

    public static AuditDecision block(String reason) {
        return new AuditDecision(
                false,
                "BLOCK",
                1.0,
                "BLOCK",
                reason,
                List.of()
        );
    }
}