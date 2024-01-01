package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class DeathStar extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 12);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.unmodifiableMap(new EnumMap<TechnologyKind, Integer>(TechnologyKind.class) {{
        put(TechnologyKind.HYPERSPACE_TECHNOLOGY, 6);
        put(TechnologyKind.HYPERSPACE_DRIVE, 7);
        put(TechnologyKind.GRAVITON_TECHNOLOGY, 1);
      }});

  private static final Map<UnitKind, Integer> rapidFireAgainst =
      Collections.unmodifiableMap(new EnumMap<UnitKind, Integer>(UnitKind.class) {{
        put(UnitKind.SMALL_CARGO, 250);
        put(UnitKind.LARGE_CARGO, 250);
        put(UnitKind.LITTLE_FIGHTER, 200);
        put(UnitKind.HEAVY_FIGHTER, 100);
        put(UnitKind.CRUISER, 33);
        put(UnitKind.BATTLESHIP, 30);
        put(UnitKind.COLONY_SHIP, 250);
        put(UnitKind.RECYCLER, 250);
        put(UnitKind.ESPIONAGE_PROBE, 1250);
        put(UnitKind.BOMBER, 25);
        put(UnitKind.SOLAR_SATELLITE, 1250);
        put(UnitKind.DESTROYER, 5);
        put(UnitKind.BATTLECRUISER, 15);
        put(UnitKind.ROCKET_LAUNCHER, 200);
        put(UnitKind.LIGHT_LASER, 200);
        put(UnitKind.HEAVY_LASER, 100);
        put(UnitKind.GAIUS_CANNON, 50);
        put(UnitKind.ION_CANNON, 100);
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
    return new Resources(5000000.0, 4000000.0, 1000000.0);
  }

  @Override
  public int getCapacity() {
    return 1000000;
  }

  @Override
  public int getConsumption(User user) {
    return 1;
  }

  @Override
  public TechnologyKind getDrive(User user) {
    return TechnologyKind.HYPERSPACE_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    return 100;
  }

  @Override
  public double getBaseWeapons() {
    return 200000.0;
  }

  @Override
  public double getBaseShield() {
    return 50000.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
