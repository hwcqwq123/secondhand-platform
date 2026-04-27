package com.example.secondhand.auth;

import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import com.example.secondhand.user.UserStatus;

@Service
public class AppUserDetailsService implements UserDetailsService {

  private final UserRepository users;

  public AppUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User u = users.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("user not found"));

    if (u.getStatus() == UserStatus.BANNED) {
      throw new DisabledException("account is banned");
    }

    String role = u.getRole() == null || u.getRole().isBlank() ? "USER" : u.getRole();

    return new org.springframework.security.core.userdetails.User(
            u.getUsername(),
            u.getPasswordHash(),
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
    );
  }
}