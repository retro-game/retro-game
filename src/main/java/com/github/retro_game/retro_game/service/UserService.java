package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.UserSettingsDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public interface UserService {
  void create(String email, String name, String password);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByNameIgnoreCase(String name);

  @PreAuthorize("isAuthenticated()")
  long getCurrentId();

  String getName(long userId);

  @PreAuthorize("isAuthenticated()")
  String getCurrentUserName();

  @PreAuthorize("isAuthenticated()")
  UserSettingsDto getCurrentUserSettings();

  @PreAuthorize("isAuthenticated()")
  void saveCurrentUserSettings(UserSettingsDto settings);

  Optional<Locale> getCurrentUserLocaleIfAuthenticated();

  @PreAuthorize("isAuthenticated()")
  boolean isOnVacation();

  @PreAuthorize("isAuthenticated()")
  Date getVacationUntil();

  @PreAuthorize("isAuthenticated()")
  boolean canEnableVacationMode();

  @PreAuthorize("isAuthenticated()")
  void enableVacationMode();

  @PreAuthorize("isAuthenticated()")
  boolean canDisableVacationMode();

  @PreAuthorize("isAuthenticated()")
  void disableVacationMode();

  @PreAuthorize("isAuthenticated()")
  boolean isBanned();

  @PreAuthorize("hasRole('ADMIN')")
  void ban(String name, long durationDays, String reason);

  @PreAuthorize("hasRole('ADMIN')")
  void unban(String name);
}
