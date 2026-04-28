package com.example.secondhand.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 64, message = "用户名长度应为 3 到 64 位")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度应为 6 到 32 位")
        String password,

        String nickname,

        @NotBlank(message = "管理员角色不能为空")
        String role
) {
    public String safeUsername() {
        return username == null ? "" : username.trim();
    }

    public String safeNickname() {
        if (nickname == null || nickname.isBlank()) {
            return safeUsername();
        }
        return nickname.trim();
    }

    public String safeRole() {
        return AdminRole.normalize(role);
    }
}