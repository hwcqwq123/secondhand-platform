package com.example.secondhand.user;

public class UserProfileResponse {
    private Long id;
    private String username;
    private String role;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String wechatQrUrl;

    public UserProfileResponse() {
    }

    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.nickname = user.getNickname();
        this.avatarUrl = user.getAvatarUrl();
        this.bio = user.getBio();
        this.wechatQrUrl = user.getWechatQrUrl();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
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