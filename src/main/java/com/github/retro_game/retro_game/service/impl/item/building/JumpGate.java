package com.github.retro_game.retro_game.service.impl.item.building;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.Map;

public class JumpGate extends BuildingItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.LUNAR_BASE, 1);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.HYPERSPACE_TECHNOLOGY, 7);

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
    return body.getCoordinates().getKind() == CoordinatesKind.MOON;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(2000000.0, 4000000.0, 2000000.0);
  }
}
