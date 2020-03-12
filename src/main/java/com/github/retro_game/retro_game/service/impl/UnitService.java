package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.entity.User;

interface UnitService {
  int getSpeed(UnitKind kind, User user);

  double getWeapons(UnitKind kind, User user);

  double getShield(UnitKind kind, User user);

  double getArmor(UnitKind kind, User user);
}
