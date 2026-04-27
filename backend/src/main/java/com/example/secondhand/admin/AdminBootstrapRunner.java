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
            @Value("${app.admin.nickname:系统管理员}") String nickname,
            @Value("${app.admin.role:SUPER_ADMIN}") String role
    ) {
        return args -> {
            if (!enabled) {
                return;
            }

            if (username == null || username.isBlank()) {
                return;
            }

            if (users.existsByUsername(username)) {
                return;
            }

            String finalRole = AdminRole.normalize(role);
            if (!AdminRole.isAdminRole(finalRole)) {
                finalRole = AdminRole.SUPER_ADMIN;
            }

            User admin = new User();
            admin.setUsername(username.trim());
            admin.setNickname(nickname);
            admin.setPasswordHash(encoder.encode(password));
            admin.setRole(finalRole);
            admin.setStatus(UserStatus.NORMAL);

            users.save(admin);
            System.out.println("默认管理员账号已创建：" + username + "，角色：" + finalRole);
        };
    }
}