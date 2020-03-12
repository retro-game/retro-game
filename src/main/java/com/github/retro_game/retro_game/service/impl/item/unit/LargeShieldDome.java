package com.github.retro_game.retro_game.service.impl.item.unit;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;

import java.util.Collections;
import java.util.Map;

public class LargeShieldDome extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 6);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.SHIELDING_TECHNOLOGY, 6);

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
    return new Resources(50000.0, 50000.0, 0.0);
  }

  @Override
  public double getBaseWeapons() {
    return 1.0;
  }

  @Override
  public double getBaseShield() {
    return 10000.0;
  }

  @Override
  public double getBaseArmor() {
    return 100000.0;
  }
}
