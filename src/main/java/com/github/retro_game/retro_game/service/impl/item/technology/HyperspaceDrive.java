package com.github.retro_game.retro_game.service.impl.item.technology;

import com.github.retro_game.retro_game.model.entity.BuildingKind;
import com.github.retro_game.retro_game.model.entity.Resources;
import com.github.retro_game.retro_game.model.entity.TechnologyKind;

import java.util.Collections;
import java.util.Map;

public class HyperspaceDrive extends TechnologyItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.RESEARCH_LAB, 7);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.HYPERSPACE_TECHNOLOGY, 3);

  @Override
  public Map<BuildingKind, Integer> getBuildingsRequirements() {
    return buildingsRequirements;
  }

  @Override
  public Map<TechnologyKind, Integer> getTechnologiesRequirements() {
    return technologiesRequirements;
  }

  @Override
  public Resources getBaseCost() {
    return new Resources(10000.0, 20000.0, 6000.0);
  }
}
