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
                    .requestMatchers("/api/auth/**").permitAll()

                    // 放行上传后的静态图片资源
                    .requestMatchers("/uploads/**").permitAll()

                    // 用户个人中心：登录用户可用
                    .requestMatchers(HttpMethod.GET, "/api/users/me").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users/me").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users/me/payment").hasAnyRole("USER", "ADMIN")

                    // 文件上传
                    .requestMatchers(HttpMethod.POST, "/api/files/upload").hasAnyRole("USER", "ADMIN")

                    // 商品：登录用户可用
                    .requestMatchers(HttpMethod.GET, "/api/items/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/items/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAnyRole("USER", "ADMIN")

                    // 订单：登录用户可用
                    .requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")

                    // 推荐 / AI客服：登录用户可用
                    .requestMatchers("/api/recommendations/**").hasAnyRole("USER", "ADMIN")

                    // 总后台
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
  }
}