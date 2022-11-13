package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;

import java.util.Collections;
import java.util.Map;

public class RocketLauncher extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 1);

  @Override
  public Map<BuildingKind, Integer> getBuildingsRequirements() {
    return buildingsRequirements;
  }

  @Override
  public Resources getCost() {
    return new Resources(2000.0, 0.0, 0.0);
  }

  @Override
  public double getBaseWeapons() {
    return 80.0;
  }

  @Override
  public double getBaseShield() {
    return 20.0;
  }
}
