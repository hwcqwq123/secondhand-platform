package com.example.secondhand.audit;

import java.util.List;

// 新增文件：Python 审核服务返回给 Spring Boot 的结果
public record AuditRemoteResponse(
        String status,
        Boolean allowed,
        Double score,
        String label,
        String reason,
        List<String> matchedWords
) {
}