package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class LittleFighter extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 1);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.COMBUSTION_DRIVE, 1);

  private static final Map<UnitKind, Integer> rapidFireAgainst =
      Collections.unmodifiableMap(new EnumMap<UnitKind, Integer>(UnitKind.class) {{
        put(UnitKind.ESPIONAGE_PROBE, 5);
        put(UnitKind.SOLAR_SATELLITE, 5);
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
    return new Resources(3000.0, 1000.0, 0.0);
  }

  @Override
  public int getCapacity() {
    return 50;
  }

  @Override
  public int getConsumption(User user) {
    return 20;
  }

  @Override
  public TechnologyKind getDrive(User user) {
    return TechnologyKind.COMBUSTION_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    return 12500;
  }

  @Override
  public double getBaseWeapons() {
    return 50.0;
  }

  @Override
  public double getBaseShield() {
    return 10.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
