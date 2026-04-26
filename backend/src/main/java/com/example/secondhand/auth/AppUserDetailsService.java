package com.example.secondhand.auth;

import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepository users;
  public AppUserDetailsService(UserRepository users) { this.users = users; }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User u = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user not found"));
    return new org.springframework.security.core.userdetails.User(
        u.getUsername(), u.getPasswordHash(),
        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))
    );
  }
}
