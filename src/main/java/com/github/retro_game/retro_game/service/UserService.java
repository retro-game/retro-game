package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.UserSettingsDto;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public interface UserService {
  void create(String email, String name, String password);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByNameIgnoreCase(String name);

  long getCurrentId();

  String getName(long userId);

  String getCurrentUserName();

  UserSettingsDto getCurrentUserSettings();

  void saveCurrentUserSettings(UserSettingsDto settings);

  Optional<Locale> getCurrentUserLocaleIfAuthenticated();

  boolean isOnVacation();

  Date getVacationUntil();

  boolean canEnableVacationMode();

  void enableVacationMode();

  boolean canDisableVacationMode();

  void disableVacationMode();

  boolean isBanned();

  void ban(String name, long durationDays, String reason);

  void unban(String name);
}
