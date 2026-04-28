package com.example.secondhand.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import com.example.secondhand.user.UserStatus;

@Configuration
public class AdminBootstrapRunner {

    @Bean
    public CommandLineRunner initDefaultAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.enabled:true}") boolean enabled,
            @Value("${app.admin.username:admin}") String username,
            @Value("${app.admin.password:admin123456}") String password,
            @Value("${app.admin.nickname:系统管理员}") String nickname
    ) {
        return args -> {
            if (!enabled) {
                return;
            }

            String fixedUsername = (username == null || username.isBlank()) ? "admin" : username.trim();
            String fixedPassword = (password == null || password.isBlank()) ? "admin123456" : password;
            String fixedNickname = (nickname == null || nickname.isBlank()) ? "系统管理员" : nickname.trim();

            User superAdmin = users.findByUsername(fixedUsername).orElseGet(User::new);
            boolean isNew = superAdmin.getId() == null;

            superAdmin.setUsername(fixedUsername);
            superAdmin.setNickname(fixedNickname);
            superAdmin.setPasswordHash(encoder.encode(fixedPassword));
            superAdmin.setRole(AdminRole.SUPER_ADMIN);
            superAdmin.setStatus(UserStatus.NORMAL);
            users.save(superAdmin);

            users.findAll().stream()
                    .filter(u -> u.getUsername() != null)
                    .filter(u -> !fixedUsername.equalsIgnoreCase(u.getUsername()))
                    .filter(u -> AdminRole.SUPER_ADMIN.equals(AdminRole.normalize(u.getRole())))
                    .forEach(u -> {
                        u.setRole(AdminRole.ADMIN);
                        users.save(u);
                    });

            if (isNew) {
                System.out.println("固定超级管理员账号已创建：" + fixedUsername + "，角色：" + AdminRole.SUPER_ADMIN);
            } else {
                System.out.println("固定超级管理员账号已校正：" + fixedUsername + "，角色：" + AdminRole.SUPER_ADMIN);
            }
        };
    }
}