package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Battleship extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 7);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.HYPERSPACE_DRIVE, 4);

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
    return new Resources(45000.0, 15000.0, 0.0);
  }

  @Override
  public int getCapacity() {
    return 1500;
  }

  @Override
  public int getConsumption(User user) {
    return 500;
  }

  @Override
  public TechnologyKind getDrive(User user) {
    return TechnologyKind.HYPERSPACE_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    return 10000;
  }

  @Override
  public double getBaseWeapons() {
    return 1000.0;
  }

  @Override
  public double getBaseShield() {
    return 200.0;
  }

  @Override
  public double getBaseArmor() {
    return 60000.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
