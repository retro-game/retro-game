package com.github.retro_game.retro_game.service.impl.item.unit;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;

import java.util.Collections;
import java.util.Map;

public class SmallShieldDome extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 1);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.SHIELDING_TECHNOLOGY, 2);

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
    return new Resources(10000.0, 10000.0, 0.0);
  }

  @Override
  public double getBaseWeapons() {
    return 1.0;
  }

  @Override
  public double getBaseShield() {
    return 2000.0;
  }

  @Override
  public double getBaseArmor() {
    return 20000.0;
  }
}
