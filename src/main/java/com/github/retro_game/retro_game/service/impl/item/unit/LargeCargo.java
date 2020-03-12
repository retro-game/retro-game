package com.github.retro_game.retro_game.service.impl.item.unit;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class LargeCargo extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 4);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.COMBUSTION_DRIVE, 6);

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
    return new Resources(6000.0, 6000.0, 0.0);
  }

  @Override
  public int getCapacity() {
    return 25000;
  }

  @Override
  public int getConsumption(User user) {
    return 50;
  }

  @Override
  public TechnologyKind getDrive(User user) {
    return TechnologyKind.COMBUSTION_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    return 7500;
  }

  @Override
  public double getBaseWeapons() {
    return 5.0;
  }

  @Override
  public double getBaseShield() {
    return 25.0;
  }

  @Override
  public double getBaseArmor() {
    return 12000.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
