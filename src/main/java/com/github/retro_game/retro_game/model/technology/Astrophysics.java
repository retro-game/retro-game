package com.github.retro_game.retro_game.model.technology;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Astrophysics extends TechnologyItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.RESEARCH_LAB, 3);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.unmodifiableMap(new EnumMap<TechnologyKind, Integer>(TechnologyKind.class) {{
        put(TechnologyKind.ESPIONAGE_TECHNOLOGY, 4);
        put(TechnologyKind.IMPULSE_DRIVE, 3);
      }});

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
    return new Resources(4000.0, 8000.0, 4000.0);
  }

  @Override
  public double getCostFactor() {
    return 1.75;
  }
}
