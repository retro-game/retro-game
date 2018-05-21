package com.github.retro_game.retro_game.service.impl.item.unit;

import com.github.retro_game.retro_game.model.entity.BuildingKind;
import com.github.retro_game.retro_game.model.entity.Resources;
import com.github.retro_game.retro_game.model.entity.TechnologyKind;

import java.util.Collections;
import java.util.Map;

public class IonCannon extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 4);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.ION_TECHNOLOGY, 4);

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
    return new Resources(2000.0, 6000.0, 0.0);
  }

  @Override
  public double getBaseWeapons() {
    return 150.0;
  }

  @Override
  public double getBaseShield() {
    return 500.0;
  }

  @Override
  public double getBaseArmor() {
    return 8000.0;
  }
}
