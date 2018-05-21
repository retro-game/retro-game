package com.github.retro_game.retro_game.service.impl.item.building;

import com.github.retro_game.retro_game.model.entity.Resources;

public class AllianceDepot extends BuildingItem {
  @Override
  public Resources getBaseCost() {
    return new Resources(20000.0, 40000.0, 0.0);
  }
}
