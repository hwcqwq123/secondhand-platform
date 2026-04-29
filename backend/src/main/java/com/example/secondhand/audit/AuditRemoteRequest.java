package com.example.secondhand.audit;

// 新增文件：Spring Boot 调用 Python 审核服务时发送的请求体
public record AuditRemoteRequest(
        String text,
        String scene
) {
}