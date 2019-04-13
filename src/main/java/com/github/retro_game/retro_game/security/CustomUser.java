package com.github.retro_game.retro_game.security;

import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.entity.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

public class CustomUser extends org.springframework.security.core.userdetails.User {
  private final long userId;

  CustomUser(User user) {
    super(user.getEmail(), user.getPassword(), createAuthorities(user));
    this.userId = user.getId();
  }

  private static ArrayList<SimpleGrantedAuthority> createAuthorities(User user) {
    int roles = user.getRoles();
    assert (roles & ~(UserRole.USER | UserRole.ADMIN)) == 0;
    ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>(Integer.bitCount(roles));
    if (user.hasRole(UserRole.USER)) {
      authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }
    if (user.hasRole(UserRole.ADMIN)) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    return authorities;
  }

  public static boolean isCurrentUserAdmin() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
  }

  public static long getCurrentUserId() {
    return ((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
  }

  public long getUserId() {
    return userId;
  }
}
