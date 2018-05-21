package com.github.retro_game.retro_game.security;

import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

public class CustomUser extends org.springframework.security.core.userdetails.User {
  private final long userId;

  CustomUser(User user) {
    super(user.getEmail(), user.getPassword(), new ArrayList<>());
    this.userId = user.getId();
  }

  public static long getCurrentUserId() {
    return ((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
  }

  public long getUserId() {
    return userId;
  }
}
