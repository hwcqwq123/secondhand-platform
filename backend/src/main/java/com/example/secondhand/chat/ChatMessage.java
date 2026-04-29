package com.example.secondhand.chat;

import java.time.Instant;

import com.example.secondhand.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// 新增：聊天消息实体
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 新增：消息所属会话
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    // 新增：消息发送人
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // 新增：消息正文
    @Column(nullable = false, length = 1000)
    private String content;

    // 新增：消息审核状态，PASS / REVIEW / BLOCK
    @Column(name = "audit_status", nullable = false, length = 20)
    private String auditStatus = "PASS";

    // 新增：审核原因
    @Column(name = "audit_reason", length = 500)
    private String auditReason;

    // 新增：审核分数
    @Column(name = "audit_score")
    private Double auditScore = 0.0;

    // 新增：是否已读
    @Column(name = "read_flag", nullable = false)
    private Boolean readFlag = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public ChatConversation getConversation() {
        return conversation;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public String getAuditReason() {
        return auditReason;
    }

    public Double getAuditScore() {
        return auditScore;
    }

    public Boolean getReadFlag() {
        return readFlag;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setConversation(ChatConversation conversation) {
        this.conversation = conversation;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }

    public void setAuditScore(Double auditScore) {
        this.auditScore = auditScore;
    }

    public void setReadFlag(Boolean readFlag) {
        this.readFlag = readFlag;
    }
}