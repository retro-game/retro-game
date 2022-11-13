package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;
import com.github.retro_game.retro_game.entity.User;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class EspionageProbe extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 3);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.unmodifiableMap(new EnumMap<TechnologyKind, Integer>(TechnologyKind.class) {{
        put(TechnologyKind.ESPIONAGE_TECHNOLOGY, 2);
        put(TechnologyKind.COMBUSTION_DRIVE, 3);
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
    return new Resources(0.0, 1000.0, 0.0);
  }

  @Override
  public int getCapacity() {
    return 5;
  }

  @Override
  public int getConsumption(User user) {
    return 1;
  }

  @Override
  public TechnologyKind getDrive(User user) {
    return TechnologyKind.COMBUSTION_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    return 100000000;
  }

  @Override
  public double getBaseWeapons() {
    return 0.01;
  }

  @Override
  public double getBaseShield() {
    return 0.01;
  }
}
