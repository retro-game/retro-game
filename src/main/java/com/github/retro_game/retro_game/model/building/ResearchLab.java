package com.github.retro_game.retro_game.model.building;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.Resources;

public class ResearchLab extends BuildingItem {
  @Override
  public boolean meetsSpecialRequirements(Body body) {
    return body.getCoordinates().getKind() == CoordinatesKind.PLANET;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(200.0, 400.0, 200.0);
  }
}
