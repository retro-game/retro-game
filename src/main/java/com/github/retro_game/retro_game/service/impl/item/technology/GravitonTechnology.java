package com.github.retro_game.retro_game.service.impl.item.technology;

import com.github.retro_game.retro_game.model.entity.BuildingKind;
import com.github.retro_game.retro_game.model.entity.Resources;

import java.util.Collections;
import java.util.Map;

public class GravitonTechnology extends TechnologyItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.RESEARCH_LAB, 12);

  @Override
  public Map<BuildingKind, Integer> getBuildingsRequirements() {
    return buildingsRequirements;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(0.0, 0.0, 0.0);
  }

  @Override
  public int getBaseRequiredEnergy() {
    return 300000;
  }

  @Override
  public double getCostFactor() {
    return 3.0;
  }
}
