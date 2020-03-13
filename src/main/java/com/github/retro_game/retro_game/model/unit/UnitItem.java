package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.model.Item;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public abstract class UnitItem extends Item {
  private static final Map<UnitKind, UnitItem> fleet =
      Collections.unmodifiableMap(new EnumMap<UnitKind, UnitItem>(UnitKind.class) {{
        put(UnitKind.SMALL_CARGO, new SmallCargo());
        put(UnitKind.LARGE_CARGO, new LargeCargo());
        put(UnitKind.LITTLE_FIGHTER, new LittleFighter());
        put(UnitKind.HEAVY_FIGHTER, new HeavyFighter());
        put(UnitKind.CRUISER, new Cruiser());
        put(UnitKind.BATTLESHIP, new Battleship());
        put(UnitKind.COLONY_SHIP, new ColonyShip());
        put(UnitKind.RECYCLER, new Recycler());
        put(UnitKind.ESPIONAGE_PROBE, new EspionageProbe());
        put(UnitKind.BOMBER, new Bomber());
        put(UnitKind.SOLAR_SATELLITE, new SolarSatellite());
        put(UnitKind.DESTROYER, new Destroyer());
        put(UnitKind.DEATH_STAR, new DeathStar());
      }});

  private static final Map<UnitKind, UnitItem> defense =
      Collections.unmodifiableMap(new EnumMap<UnitKind, UnitItem>(UnitKind.class) {{
        put(UnitKind.ROCKET_LAUNCHER, new RocketLauncher());
        put(UnitKind.LIGHT_LASER, new LightLaser());
        put(UnitKind.HEAVY_LASER, new HeavyLaser());
        put(UnitKind.GAIUS_CANNON, new GaiusCannon());
        put(UnitKind.ION_CANNON, new IonCannon());
        put(UnitKind.PLASMA_TURRET, new PlasmaTurret());
        put(UnitKind.SMALL_SHIELD_DOME, new SmallShieldDome());
        put(UnitKind.LARGE_SHIELD_DOME, new LargeShieldDome());
        put(UnitKind.ANTI_BALLISTIC_MISSILE, new AntiBallisticMissile());
        put(UnitKind.INTERPLANETARY_MISSILE, new InterplanetaryMissile());
      }});

  private static final Map<UnitKind, UnitItem> all =
      Collections.unmodifiableMap(new EnumMap<UnitKind, UnitItem>(UnitKind.class) {{
        putAll(fleet);
        putAll(defense);
      }});

  public static Map<UnitKind, UnitItem> getFleet() {
    return fleet;
  }

  public static Map<UnitKind, UnitItem> getDefense() {
    return defense;
  }

  public static Map<UnitKind, UnitItem> getAll() {
    return all;
  }

  public abstract Resources getCost();

  public int getCapacity() {
    return 0;
  }

  public int getConsumption(User user) {
    return 0;
  }

  @Nullable
  public TechnologyKind getDrive(User user) {
    return null;
  }

  public int getBaseSpeed(User user) {
    return 0;
  }

  public abstract double getBaseWeapons();

  public abstract double getBaseShield();

  public abstract double getBaseArmor();

  public Map<UnitKind, Integer> getRapidFireAgainst() {
    return Collections.emptyMap();
  }
}
