package com.example.secondhand.audit;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// 新增文件：内容审核服务
// 作用：商品发布 / 商品编辑 / 后续聊天消息，都可以统一调用这个类。
@Service
public class ContentAuditService {

    private final RestTemplate auditRestTemplate;

    @Value("${app.audit.enabled:true}")
    private boolean enabled;

    @Value("${app.audit.service-url:http://127.0.0.1:8001}")
    private String serviceUrl;

    @Value("${app.audit.fail-open:true}")
    private boolean failOpen;

    public ContentAuditService(RestTemplate auditRestTemplate) {
        this.auditRestTemplate = auditRestTemplate;
    }

    public AuditDecision auditItem(String title, String description) {
        String text = """
                商品标题：%s
                商品描述：%s
                """.formatted(
                safe(title),
                safe(description)
        );

        return audit(text, "ITEM");
    }

    public AuditDecision auditChatMessage(String content) {
        return audit(safe(content), "CHAT");
    }

    public AuditDecision audit(String text, String scene) {
        if (!enabled) {
            return AuditDecision.pass("内容审核未启用");
        }

        if (text == null || text.isBlank()) {
            return AuditDecision.pass("空文本无需审核");
        }

        String baseUrl = normalizeBaseUrl(serviceUrl);
        String url = baseUrl + "/audit";

        try {
            AuditRemoteRequest request = new AuditRemoteRequest(text, scene);

            ResponseEntity<AuditRemoteResponse> response =
                    auditRestTemplate.postForEntity(url, request, AuditRemoteResponse.class);

            AuditRemoteResponse remote = response.getBody();

            if (remote == null) {
                return handleAuditServiceError("AI 审核服务返回为空");
            }

            boolean allowed = Boolean.TRUE.equals(remote.allowed());
            String status = remote.status() == null ? "REVIEW" : remote.status();
            double score = remote.score() == null ? 0.0 : remote.score();
            String label = remote.label() == null ? "" : remote.label();
            String reason = remote.reason() == null ? "内容审核未通过" : remote.reason();
            List<String> matchedWords = remote.matchedWords() == null ? List.of() : remote.matchedWords();

            return new AuditDecision(
                    allowed,
                    status,
                    score,
                    label,
                    reason,
                    matchedWords
            );

        } catch (Exception e) {
            return handleAuditServiceError("AI 审核服务调用失败：" + e.getMessage());
        }
    }

    private AuditDecision handleAuditServiceError(String message) {
        if (failOpen) {
            // 修改点：本地开发默认放行，避免 Python 审核服务没启动时影响正常开发
            return AuditDecision.pass(message + "，本地开发模式已放行");
        }

        // 修改点：上线后建议 fail-open 改为 false，审核服务异常时阻止发布
        return AuditDecision.block(message);
    }

    private String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://127.0.0.1:8001";
        }
        return url.replaceAll("/+$", "");
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}