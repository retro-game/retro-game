package com.github.retro_game.retro_game.service.impl.item.unit;

import com.github.retro_game.retro_game.model.entity.BuildingKind;
import com.github.retro_game.retro_game.model.entity.Resources;

import java.util.Collections;
import java.util.Map;

public class AntiBallisticMissile extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.MISSILE_SILO, 2);

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

  @Override
  public double getBaseArmor() {
    return 8000.0;
  }
}
