package com.example.secondhand.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expiresMinutes;

  public JwtService(@Value("${app.jwt.secret}") String base64Secret,
                    @Value("${app.jwt.expires-minutes}") long expiresMinutes) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    this.expiresMinutes = expiresMinutes;
  }

  public String generateToken(String subject, String role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expiresMinutes * 60);
    return Jwts.builder()
        .subject(subject)
        .claim("role", role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public String extractUsername(String token) {
    return Jwts.parser().verifyWith(key).build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}
