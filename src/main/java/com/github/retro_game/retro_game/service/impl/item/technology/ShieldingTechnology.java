package com.github.retro_game.retro_game.service.impl.item.technology;

import com.github.retro_game.retro_game.model.entity.BuildingKind;
import com.github.retro_game.retro_game.model.entity.Resources;
import com.github.retro_game.retro_game.model.entity.TechnologyKind;

import java.util.Collections;
import java.util.Map;

public class ShieldingTechnology extends TechnologyItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.RESEARCH_LAB, 6);

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
  public Resources getBaseCost() {
    return new Resources(200.0, 600.0, 0.0);
  }
}
