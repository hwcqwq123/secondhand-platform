package com.example.secondhand.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import com.example.secondhand.user.UserStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;
  private final JwtService jwtService;

  public AuthController(
          UserRepository users,
          PasswordEncoder encoder,
          AuthenticationManager authManager,
          JwtService jwtService
  ) {
    this.users = users;
    this.encoder = encoder;
    this.authManager = authManager;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
    if (users.existsByUsername(req.username())) {
      return ApiResponse.fail("username already exists");
    }

    User u = new User();
    u.setUsername(req.username());
    u.setNickname(req.username());
    u.setPasswordHash(encoder.encode(req.password()));
    u.setRole("USER");
    u.setStatus(UserStatus.NORMAL);

    u = users.save(u);

    String token = jwtService.generateToken(u.getUsername(), u.getRole());
    return ApiResponse.ok(
            new AuthResponse(token, u.getId(), u.getUsername(), u.getRole()),
            "registered"
    );
  }

  @PostMapping("/login")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
    authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
    );

    User u = users.findByUsername(req.username()).orElseThrow();

    if (u.getStatus() == UserStatus.BANNED) {
      return ApiResponse.fail("账号已被封禁，无法登录");
    }

    String token = jwtService.generateToken(u.getUsername(), u.getRole());
    return ApiResponse.ok(
            new AuthResponse(token, u.getId(), u.getUsername(), u.getRole()),
            "logged in"
    );
  }
}