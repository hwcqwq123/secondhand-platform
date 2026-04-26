package com.example.secondhand.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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

  @Column(nullable = false, length = 32)
  private String role = "USER";

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