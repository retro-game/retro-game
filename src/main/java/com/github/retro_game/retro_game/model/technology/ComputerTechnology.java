package com.github.retro_game.retro_game.model.technology;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;

import java.util.Collections;
import java.util.Map;

public class ComputerTechnology extends TechnologyItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.RESEARCH_LAB, 1);

  @Override
  public Map<BuildingKind, Integer> getBuildingsRequirements() {
    return buildingsRequirements;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(0.0, 400.0, 600.0);
  }
}
