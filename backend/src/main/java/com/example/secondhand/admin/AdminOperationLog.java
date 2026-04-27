package com.example.secondhand.admin;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "admin_operation_logs")
public class AdminOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_username", nullable = false, length = 64)
    private String adminUsername;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(length = 1000)
    private String detail;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getDetail() {
        return detail;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}