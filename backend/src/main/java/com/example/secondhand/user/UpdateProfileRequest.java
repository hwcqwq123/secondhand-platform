package com.example.secondhand.user;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址长度不能超过255个字符")
    private String avatarUrl;

    @Size(max = 255, message = "个人简介长度不能超过255个字符")
    private String bio;

    public String getNickname() {
        return nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getBio() {
        return bio;
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
}