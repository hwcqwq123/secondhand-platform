package com.example.secondhand.auth;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        String role
) {}