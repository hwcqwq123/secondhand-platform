package com.example.secondhand.admin;

import java.util.Optional;

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

            String finalRole = AdminRole.normalize(role);
            if (!AdminRole.isAdminRole(finalRole)) {
                finalRole = AdminRole.SUPER_ADMIN;
            }

            long superAdminCount = users.countByRole(AdminRole.SUPER_ADMIN);

            if (superAdminCount > 1) {
                System.out.println("警告：数据库中存在多个 SUPER_ADMIN，请人工检查 users 表。");
            }

            Optional<User> existingUserOpt = users.findByUsername(username.trim());

            /**
             * 情况一：
             * 配置想创建 SUPER_ADMIN，但是系统中已经有 SUPER_ADMIN。
             * 此时不能再创建第二个 SUPER_ADMIN。
             */
            if (AdminRole.SUPER_ADMIN.equals(finalRole) && superAdminCount > 0) {
                System.out.println("系统已存在 SUPER_ADMIN，跳过默认超级管理员创建。");
                return;
            }

            /**
             * 情况二：
             * 配置用户名已经存在，并且当前系统还没有 SUPER_ADMIN。
             * 则把这个配置用户升级为唯一 SUPER_ADMIN。
             */
            if (existingUserOpt.isPresent()) {
                User existing = existingUserOpt.get();

                if (AdminRole.SUPER_ADMIN.equals(finalRole) && superAdminCount == 0) {
                    existing.setRole(AdminRole.SUPER_ADMIN);
                    existing.setStatus(UserStatus.NORMAL);
                    existing.setPasswordHash(encoder.encode(password));
                    existing.setNickname(nickname);
                    users.save(existing);

                    System.out.println("已将现有账号升级为唯一 SUPER_ADMIN：" + username);
                    return;
                }

                System.out.println("默认管理员账号已存在，跳过创建：" + username);
                return;
            }

            /**
             * 情况三：
             * 用户名不存在，且没有 SUPER_ADMIN，则创建默认 SUPER_ADMIN。
             */
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