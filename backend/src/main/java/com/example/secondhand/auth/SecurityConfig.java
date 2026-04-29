package com.example.secondhand.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
public class SecurityConfig {

    // 新增/修复：提供 PasswordEncoder Bean
    // AuthController 注册用户、登录认证时都需要它
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 新增/修复：配置认证提供者
    // 使用你项目里的 AppUserDetailsService + BCrypt 密码加密
    @Bean
    public DaoAuthenticationProvider authProvider(
            AppUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // 新增/修复：暴露 AuthenticationManager
    // AuthController 登录时需要 authManager.authenticate(...)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 新增/修复：Spring Security 权限配置
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // 登录、注册、管理员注册接口放行
                        .requestMatchers("/", "/api/auth/**").permitAll()

                        // Redis 测试接口放行
                        .requestMatchers("/api/redis/ping").permitAll()

                        // 上传图片访问放行
                        .requestMatchers("/uploads/**").permitAll()

                        // 当前用户信息
                        .requestMatchers(HttpMethod.GET, "/api/users/me")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        .requestMatchers(HttpMethod.PUT, "/api/users/me")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        .requestMatchers(HttpMethod.PUT, "/api/users/me/payment")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        // 文件上传
                        .requestMatchers(HttpMethod.POST, "/api/files/upload")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        .requestMatchers(HttpMethod.POST, "/api/files/delete-unbound")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        // 商品列表、商品详情允许游客访问
                        .requestMatchers(HttpMethod.GET, "/api/items").permitAll()
                        .requestMatchers(
                                new RegexRequestMatcher("^/api/items/\\d+$", "GET")
                        ).permitAll()

                        // 商品发布、修改、删除需要登录
                        .requestMatchers(HttpMethod.POST, "/api/items/**")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        .requestMatchers(HttpMethod.PUT, "/api/items/**")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        .requestMatchers(HttpMethod.DELETE, "/api/items/**")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        // 订单接口
                        .requestMatchers("/api/orders/**")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        // 推荐接口
                        .requestMatchers("/api/recommendations/**")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        // 新增/修复：AI 客服接口
                        .requestMatchers("/api/ai/**")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        // 新增/修复：用户聊天接口
                        .requestMatchers("/api/chats/**")
                        .hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        // 管理端：商品管理员
                        .requestMatchers("/api/admin/items/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "ITEM_ADMIN")

                        // 管理端：订单管理员
                        .requestMatchers("/api/admin/orders/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "ORDER_ADMIN")

                        // 管理端：用户管理员
                        .requestMatchers("/api/admin/users/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "USER_ADMIN")

                        // 管理端：日志管理员
                        .requestMatchers("/api/admin/logs/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "SYSTEM_ADMIN")

                        // 管理端：首页统计
                        .requestMatchers("/api/admin/stats/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN",
                                "ITEM_ADMIN",
                                "ORDER_ADMIN",
                                "USER_ADMIN",
                                "SYSTEM_ADMIN"
                        )

                        // 其他管理端接口
                        .requestMatchers("/api/admin/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 其他请求都需要登录
                        .anyRequest().authenticated()
                )

                // JWT 过滤器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}