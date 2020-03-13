package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class HeavyFighter extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 3);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.unmodifiableMap(new EnumMap<TechnologyKind, Integer>(TechnologyKind.class) {{
        put(TechnologyKind.ARMOR_TECHNOLOGY, 2);
        put(TechnologyKind.IMPULSE_DRIVE, 2);
      }});

  private static final Map<UnitKind, Integer> rapidFireAgainst =
      Collections.unmodifiableMap(new EnumMap<UnitKind, Integer>(UnitKind.class) {{
        put(UnitKind.SMALL_CARGO, 3);
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
    return new Resources(6000.0, 4000.0, 0.0);
  }

  @Override
  public int getCapacity() {
    return 100;
  }

  @Override
  public int getConsumption(User user) {
    return 75;
  }

  @Override
  public TechnologyKind getDrive(User user) {
    return TechnologyKind.IMPULSE_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    return 10000;
  }

  @Override
  public double getBaseWeapons() {
    return 150.0;
  }

  @Override
  public double getBaseShield() {
    return 25.0;
  }

  @Override
  public double getBaseArmor() {
    return 10000.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
