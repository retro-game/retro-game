package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class LightLaser extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 2);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.unmodifiableMap(new EnumMap<TechnologyKind, Integer>(TechnologyKind.class) {{
        put(TechnologyKind.ENERGY_TECHNOLOGY, 1);
        put(TechnologyKind.LASER_TECHNOLOGY, 3);
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
  public Resources getCost() {
    return new Resources(1500.0, 500.0, 0.0);
  }

  @Override
  public double getBaseWeapons() {
    return 100.0;
  }

  @Override
  public double getBaseShield() {
    return 25.0;
  }

  @Override
  public double getBaseArmor() {
    return 2000.0;
  }
}
