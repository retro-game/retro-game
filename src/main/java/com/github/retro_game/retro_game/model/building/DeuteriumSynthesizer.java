package com.github.retro_game.retro_game.model.building;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.Resources;

public class DeuteriumSynthesizer extends BuildingItem {
  @Override
  public boolean meetsSpecialRequirements(Body body) {
    return body.getCoordinates().getKind() == CoordinatesKind.PLANET;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(225.0, 75.0, 0.0);
  }

  @Override
  public double getCostFactor() {
    return 1.5;
  }
}
