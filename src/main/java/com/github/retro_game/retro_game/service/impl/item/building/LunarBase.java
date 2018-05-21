package com.github.retro_game.retro_game.service.impl.item.building;

import com.github.retro_game.retro_game.model.entity.Body;
import com.github.retro_game.retro_game.model.entity.CoordinatesKind;
import com.github.retro_game.retro_game.model.entity.Resources;

public class LunarBase extends BuildingItem {
  @Override
  public boolean meetsSpecialRequirements(Body body) {
    return body.getCoordinates().getKind() == CoordinatesKind.MOON;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(20000.0, 40000.0, 20000.0);
  }
}
