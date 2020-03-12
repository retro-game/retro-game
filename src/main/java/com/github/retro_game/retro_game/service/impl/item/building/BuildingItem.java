package com.github.retro_game.retro_game.service.impl.item.building;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.service.impl.item.Item;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public abstract class BuildingItem extends Item {
  private static final Map<BuildingKind, BuildingItem> buildings =
      Collections.unmodifiableMap(new EnumMap<BuildingKind, BuildingItem>(BuildingKind.class) {{
        put(BuildingKind.METAL_MINE, new MetalMine());
        put(BuildingKind.CRYSTAL_MINE, new CrystalMine());
        put(BuildingKind.DEUTERIUM_SYNTHESIZER, new DeuteriumSynthesizer());
        put(BuildingKind.SOLAR_PLANT, new SolarPlant());
        put(BuildingKind.FUSION_REACTOR, new FusionReactor());
        put(BuildingKind.ROBOTICS_FACTORY, new RoboticsFactory());
        put(BuildingKind.NANITE_FACTORY, new NaniteFactory());
        put(BuildingKind.SHIPYARD, new Shipyard());
        put(BuildingKind.METAL_STORAGE, new MetalStorage());
        put(BuildingKind.CRYSTAL_STORAGE, new CrystalStorage());
        put(BuildingKind.DEUTERIUM_TANK, new DeuteriumTank());
        put(BuildingKind.RESEARCH_LAB, new ResearchLab());
        put(BuildingKind.TERRAFORMER, new Terraformer());
        put(BuildingKind.ALLIANCE_DEPOT, new AllianceDepot());
        put(BuildingKind.LUNAR_BASE, new LunarBase());
        put(BuildingKind.SENSOR_PHALANX, new SensorPhalanx());
        put(BuildingKind.JUMP_GATE, new JumpGate());
        put(BuildingKind.MISSILE_SILO, new MissileSilo());
      }});

  public static Map<BuildingKind, BuildingItem> getAll() {
    return buildings;
  }

  public boolean meetsSpecialRequirements(Body body) {
    return true;
  }

  public abstract Resources getBaseCost();

  public int getBaseRequiredEnergy() {
    return 0;
  }

  public double getCostFactor() {
    return 2.0;
  }
}
