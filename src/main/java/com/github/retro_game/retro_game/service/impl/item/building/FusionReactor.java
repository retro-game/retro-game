package com.github.retro_game.retro_game.service.impl.item.building;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.Map;

public class FusionReactor extends BuildingItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.DEUTERIUM_SYNTHESIZER, 5);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.ENERGY_TECHNOLOGY, 3);

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
    return new Resources(900.0, 360.0, 180.0);
  }

  @Override
  public double getCostFactor() {
    return 1.8;
  }
}
