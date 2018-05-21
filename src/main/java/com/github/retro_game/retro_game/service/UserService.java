package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.UserSettingsDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Locale;
import java.util.Optional;

public interface UserService {
  void create(String email, String name, String password);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByNameIgnoreCase(String name);

  String getName(long userId);

  @PreAuthorize("isAuthenticated()")
  String getCurrentUserName();

  @PreAuthorize("isAuthenticated()")
  UserSettingsDto getCurrentUserSettings();

  @PreAuthorize("isAuthenticated()")
  void saveCurrentUserSettings(UserSettingsDto settings);

  Optional<Locale> getCurrentUserLocaleIfAuthenticated();
}
