package com.github.retro_game.retro_game.service.impl.item.unit;

import com.github.retro_game.retro_game.model.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Destroyer extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 9);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.unmodifiableMap(new EnumMap<TechnologyKind, Integer>(TechnologyKind.class) {{
        put(TechnologyKind.HYPERSPACE_DRIVE, 6);
        put(TechnologyKind.HYPERSPACE_TECHNOLOGY, 5);
      }});

  private static final Map<UnitKind, Integer> rapidFireAgainst =
      Collections.unmodifiableMap(new EnumMap<UnitKind, Integer>(UnitKind.class) {{
        put(UnitKind.ESPIONAGE_PROBE, 5);
        put(UnitKind.SOLAR_SATELLITE, 5);
        put(UnitKind.LIGHT_LASER, 10);
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
    return new Resources(60000.0, 50000.0, 15000.0);
  }

  @Override
  public int getCapacity() {
    return 2000;
  }

  @Override
  public int getConsumption(User user) {
    return 1000;
  }

  @Override
  public TechnologyKind getDrive(User user) {
    return TechnologyKind.HYPERSPACE_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    return 5000;
  }

  @Override
  public double getBaseWeapons() {
    return 2000.0;
  }

  @Override
  public double getBaseShield() {
    return 500.0;
  }

  @Override
  public double getBaseArmor() {
    return 110000.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
