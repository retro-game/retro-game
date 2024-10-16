package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.*;

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
	put(UnitKind.CRUISER, 3);
	put(UnitKind.BATTLESHIP, 2);
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
    return new Resources(80000.0, 60000.0, 40000.0);
  }

  @Override
  public int getCapacity() {
    return 3000;
  }

  @Override
  public int getConsumption(User user) {
    return 750;
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
    return 2500.0;
  }

  @Override
  public double getBaseShield() {
    return 750.0;
  }

  @Override
  public double getBaseArmor() {
    return 150000.0;
  }

  @Override
  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }
}
