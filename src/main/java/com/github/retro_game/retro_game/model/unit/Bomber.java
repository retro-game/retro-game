package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Bomber extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 8);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.unmodifiableMap(new EnumMap<TechnologyKind, Integer>(TechnologyKind.class) {{
        put(TechnologyKind.IMPULSE_DRIVE, 6);
        put(TechnologyKind.PLASMA_TECHNOLOGY, 5);
      }});

  private static final Map<UnitKind, Integer> rapidFireAgainst =
      Collections.unmodifiableMap(new EnumMap<UnitKind, Integer>(UnitKind.class) {{
        put(UnitKind.ESPIONAGE_PROBE, 5);
        put(UnitKind.SOLAR_SATELLITE, 5);
        put(UnitKind.ROCKET_LAUNCHER, 20);
        put(UnitKind.LIGHT_LASER, 20);
        put(UnitKind.HEAVY_LASER, 10);
        put(UnitKind.ION_CANNON, 10);
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
    return new Resources(50000.0, 25000.0, 15000.0);
  }

  @Override
  public int getCapacity() {
    return 500;
  }

  @Override
  public int getConsumption(User user) {
    return 1000;
  }

  @Override
  public TechnologyKind getDrive(User user) {
    int level = user.getTechnologyLevel(TechnologyKind.HYPERSPACE_DRIVE);
    return level >= 8 ? TechnologyKind.HYPERSPACE_DRIVE : TechnologyKind.IMPULSE_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    int level = user.getTechnologyLevel(TechnologyKind.HYPERSPACE_DRIVE);
    return level >= 8 ? 5000 : 4000;
  }

  @Override
  public double getBaseWeapons() {
    return 1000.0;
  }

  @Override
  public double getBaseShield() {
    return 500.0;
  }

  @Override
  public double getBaseArmor() {
    return 75000.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
