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

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authProvider(AppUserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(encoder);
    return p;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
    return cfg.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/api/auth/**").permitAll()
                    .requestMatchers("/api/redis/ping").permitAll()
                    .requestMatchers("/uploads/**").permitAll()

                    .requestMatchers(HttpMethod.GET, "/api/users/me").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users/me").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users/me/payment").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")

                    .requestMatchers(HttpMethod.POST, "/api/files/upload").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/files/delete-unbound").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")

                    .requestMatchers(HttpMethod.GET, "/api/items").permitAll()
                    .requestMatchers(new RegexRequestMatcher("^/api/items/\\d+$", "GET")).permitAll()

                    .requestMatchers(HttpMethod.POST, "/api/items/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")

                    .requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers("/api/recommendations/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")

                    /**
                     * 后台接口权限细分：
                     * 不是只靠前端隐藏页面，而是在后端 SecurityConfig 中强制校验。
                     */
                    .requestMatchers("/api/admin/items/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "ITEM_ADMIN")
                    .requestMatchers("/api/admin/orders/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "ORDER_ADMIN")
                    .requestMatchers("/api/admin/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "USER_ADMIN")
                    .requestMatchers("/api/admin/logs/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers("/api/admin/stats/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "ITEM_ADMIN", "ORDER_ADMIN", "USER_ADMIN", "SYSTEM_ADMIN")

                    /**
                     * 兜底：admin 其他接口只允许老 ADMIN 和 SUPER_ADMIN。
                     */
                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
  }
}