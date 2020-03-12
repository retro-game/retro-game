package com.github.retro_game.retro_game.service.impl.item.building;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.Map;

public class NaniteFactory extends BuildingItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.ROBOTICS_FACTORY, 10);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.COMPUTER_TECHNOLOGY, 10);

  @Override
  public Map<BuildingKind, Integer> getBuildingsRequirements() {
    return buildingsRequirements;
  }

  @Override
  public Map<TechnologyKind, Integer> getTechnologiesRequirements() {
    return technologiesRequirements;
  }

  @Override
  public boolean meetsSpecialRequirements(Body body) {
    return body.getCoordinates().getKind() == CoordinatesKind.PLANET;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(1000000.0, 500000.0, 100000.0);
  }
}
