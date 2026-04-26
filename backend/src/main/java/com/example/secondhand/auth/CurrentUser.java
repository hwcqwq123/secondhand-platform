package com.example.secondhand.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
  public String username() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || !a.isAuthenticated()) return null;

    String name = a.getName();
    if (name == null || "anonymousUser".equals(name)) return null;

    return name;
  }
}