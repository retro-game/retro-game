package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class SmallCargo extends UnitItem {
  private static final Map<BuildingKind, Integer> buildingsRequirements =
      Collections.singletonMap(BuildingKind.SHIPYARD, 2);

  private static final Map<TechnologyKind, Integer> technologiesRequirements =
      Collections.singletonMap(TechnologyKind.COMBUSTION_DRIVE, 2);

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
    return new Resources(2000.0, 2000.0, 0.0);
  }

  @Override
  public int getCapacity() {
    return 5000;
  }

  @Override
  public int getConsumption(User user) {
    Technology impulseDrive = user.getTechnologies().get(TechnologyKind.IMPULSE_DRIVE);
    if (impulseDrive != null && impulseDrive.getLevel() >= 5) {
      return 20;
    } else {
      return 10;
    }
  }

  @Override
  public TechnologyKind getDrive(User user) {
    int level = user.getTechnologyLevel(TechnologyKind.IMPULSE_DRIVE);
    return level >= 5 ? TechnologyKind.IMPULSE_DRIVE : TechnologyKind.COMBUSTION_DRIVE;
  }

  @Override
  public int getBaseSpeed(User user) {
    int level = user.getTechnologyLevel(TechnologyKind.IMPULSE_DRIVE);
    return level >= 5 ? 10000 : 5000;
  }

  @Override
  public double getBaseWeapons() {
    return 5.0;
  }

  @Override
  public double getBaseShield() {
    return 10.0;
  }

  @Override
  public double getBaseArmor() {
    return 4000.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
