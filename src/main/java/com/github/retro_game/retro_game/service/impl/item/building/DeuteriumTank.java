package com.github.retro_game.retro_game.service.impl.item.building;

import com.github.retro_game.retro_game.model.entity.Body;
import com.github.retro_game.retro_game.model.entity.CoordinatesKind;
import com.github.retro_game.retro_game.model.entity.Resources;

public class DeuteriumTank extends BuildingItem {
  @Override
  public boolean meetsSpecialRequirements(Body body) {
    return body.getCoordinates().getKind() == CoordinatesKind.PLANET;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(2000.0, 2000.0, 0.0);
  }
}
