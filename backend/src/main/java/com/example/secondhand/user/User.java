package com.example.secondhand.user;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, unique = true, length = 64)
  private String username;

  @NotBlank
  @JsonIgnore
  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  /**
   * 原来只有 USER / ADMIN。
   * 现在支持：
   * USER 普通用户
   * ADMIN 老管理员兼容
   * SUPER_ADMIN 超级管理员
   * ITEM_ADMIN 商品管理员
   * ORDER_ADMIN 订单管理员
   * USER_ADMIN 用户管理员
   * SYSTEM_ADMIN 系统/日志管理员
   */
  @Column(nullable = false, length = 32)
  private String role = "USER";

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20)
  private UserStatus status = UserStatus.NORMAL;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "nickname", length = 64)
  private String nickname;

  @Column(name = "avatar_url", length = 255)
  private String avatarUrl;

  @Column(name = "bio", length = 255)
  private String bio;

  @Column(name = "wechat_qr_url", length = 255)
  private String wechatQrUrl;

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getRole() {
    return role;
  }

  public UserStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getNickname() {
    return nickname;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public String getBio() {
    return bio;
  }

  public String getWechatQrUrl() {
    return wechatQrUrl;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public void setWechatQrUrl(String wechatQrUrl) {
    this.wechatQrUrl = wechatQrUrl;
  }
}