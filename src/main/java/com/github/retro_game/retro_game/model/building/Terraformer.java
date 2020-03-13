package com.github.retro_game.retro_game.model.building;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.Map;

public class Terraformer extends BuildingItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.NANITE_FACTORY, 1);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.ENERGY_TECHNOLOGY, 12);

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
    return new Resources(0.0, 50000.0, 100000.0);
  }

  @Override
  public int getBaseRequiredEnergy() {
    return 1000;
  }
}
