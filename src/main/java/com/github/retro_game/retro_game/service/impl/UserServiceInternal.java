package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.service.UserService;

import java.util.Date;

public interface UserServiceInternal extends UserService {
  boolean checkCurrentUserPassword(String password);

  boolean isOnVacation(User user);

  boolean isBanned(Date vacationUntil, boolean forcedVacation);
}
