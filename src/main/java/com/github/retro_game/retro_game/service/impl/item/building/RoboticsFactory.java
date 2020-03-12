package com.github.retro_game.retro_game.service.impl.item.building;

import com.github.retro_game.retro_game.entity.Resources;

public class RoboticsFactory extends BuildingItem {
  @Override
  public Resources getBaseCost() {
    return new Resources(400.0, 120.0, 200.0);
  }
}
