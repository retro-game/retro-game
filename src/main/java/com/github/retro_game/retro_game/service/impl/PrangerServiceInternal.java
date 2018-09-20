package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.service.PrangerService;

import java.util.Date;

public interface PrangerServiceInternal extends PrangerService {
  void createEntry(User user, Date at, Date until, String reason, User admin);

  void deleteEntry(User user, Date until);
}
