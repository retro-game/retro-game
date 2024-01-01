package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class AntiBallisticMissile extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.unmodifiableMap(new EnumMap<BuildingKind, Integer>(BuildingKind.class) {{
        put(BuildingKind.SHIPYARD, 1);
        put(BuildingKind.MISSILE_SILO, 2);
      }});

  @Override
  public Map<BuildingKind, Integer> getBuildingsRequirements() {
    return buildingsRequirements;
  }

  @Override
  public Resources getCost() {
    return new Resources(8000.0, 0.0, 2000.0);
  }

  @Override
  public double getBaseWeapons() {
    return 1.0;
  }

  @Override
  public double getBaseShield() {
    return 1.0;
  }
}
