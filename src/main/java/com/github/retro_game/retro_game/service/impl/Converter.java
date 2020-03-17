package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Converter {
  static <KeyFrom, ValueFrom, KeyTo extends Enum<KeyTo>, ValueTo> EnumMap<KeyTo, ValueTo>
  convertToEnumMap(Map<KeyFrom, ValueFrom> map, Class<KeyTo> keyToClass, Function<KeyFrom, KeyTo> keyConverter,
                   Function<ValueFrom, ValueTo> valueConverter) {
    return map.entrySet().stream()
        .collect(Collectors.toMap(
            entry -> keyConverter.apply(entry.getKey()),
            entry -> valueConverter.apply(entry.getValue()),
            (a, b) -> {
              throw new IllegalStateException();
            },
            () -> new EnumMap<>(keyToClass)));
  }

  static BattleResultDto convert(BattleResult entity) {
    switch (entity) {
      case ATTACKERS_WIN:
        return BattleResultDto.ATTACKERS_WIN;
      case DEFENDERS_WIN:
        return BattleResultDto.DEFENDERS_WIN;
      case DRAW:
        return BattleResultDto.DRAW;
      default:
        throw new IllegalArgumentException("Illegal value for BattleResult");
    }
  }

  static BodiesSortOrderDto convert(BodiesSortOrder entity) {
    switch (entity) {
      case EMERGENCE:
        return BodiesSortOrderDto.EMERGENCE;
      case COORDINATES:
        return BodiesSortOrderDto.COORDINATES;
      case NAME:
        return BodiesSortOrderDto.NAME;
      default:
        throw new IllegalArgumentException("Illegal value for BodiesSortOrder");
    }
  }

  static BodiesSortOrder convert(BodiesSortOrderDto dto) {
    switch (dto) {
      case EMERGENCE:
        return BodiesSortOrder.EMERGENCE;
      case COORDINATES:
        return BodiesSortOrder.COORDINATES;
      case NAME:
        return BodiesSortOrder.NAME;
      default:
        throw new IllegalArgumentException("Illegal value for BodiesSortOrderDto");
    }
  }

  static BodyTypeDto convert(BodyType entity) {
    switch (entity) {
      case MOON:
        return BodyTypeDto.MOON;
      case DRY:
        return BodyTypeDto.DRY;
      case DESERT:
        return BodyTypeDto.DESERT;
      case JUNGLE:
        return BodyTypeDto.JUNGLE;
      case NORMAL:
        return BodyTypeDto.NORMAL;
      case WATER:
        return BodyTypeDto.WATER;
      case ICE:
        return BodyTypeDto.ICE;
      case GAS:
        return BodyTypeDto.GAS;
      default:
        throw new IllegalArgumentException("Illegal value for BodyType");
    }
  }

  static BuildingKindDto convert(BuildingKind entity) {
    switch (entity) {
      case METAL_MINE:
        return BuildingKindDto.METAL_MINE;
      case CRYSTAL_MINE:
        return BuildingKindDto.CRYSTAL_MINE;
      case DEUTERIUM_SYNTHESIZER:
        return BuildingKindDto.DEUTERIUM_SYNTHESIZER;
      case SOLAR_PLANT:
        return BuildingKindDto.SOLAR_PLANT;
      case FUSION_REACTOR:
        return BuildingKindDto.FUSION_REACTOR;
      case ROBOTICS_FACTORY:
        return BuildingKindDto.ROBOTICS_FACTORY;
      case NANITE_FACTORY:
        return BuildingKindDto.NANITE_FACTORY;
      case SHIPYARD:
        return BuildingKindDto.SHIPYARD;
      case METAL_STORAGE:
        return BuildingKindDto.METAL_STORAGE;
      case CRYSTAL_STORAGE:
        return BuildingKindDto.CRYSTAL_STORAGE;
      case DEUTERIUM_TANK:
        return BuildingKindDto.DEUTERIUM_TANK;
      case RESEARCH_LAB:
        return BuildingKindDto.RESEARCH_LAB;
      case TERRAFORMER:
        return BuildingKindDto.TERRAFORMER;
      case ALLIANCE_DEPOT:
        return BuildingKindDto.ALLIANCE_DEPOT;
      case LUNAR_BASE:
        return BuildingKindDto.LUNAR_BASE;
      case SENSOR_PHALANX:
        return BuildingKindDto.SENSOR_PHALANX;
      case JUMP_GATE:
        return BuildingKindDto.JUMP_GATE;
      case MISSILE_SILO:
        return BuildingKindDto.MISSILE_SILO;
      default:
        throw new IllegalArgumentException("Illegal value for BuildingKind");
    }
  }

  static BuildingKind convert(BuildingKindDto dto) {
    switch (dto) {
      case METAL_MINE:
        return BuildingKind.METAL_MINE;
      case CRYSTAL_MINE:
        return BuildingKind.CRYSTAL_MINE;
      case DEUTERIUM_SYNTHESIZER:
        return BuildingKind.DEUTERIUM_SYNTHESIZER;
      case SOLAR_PLANT:
        return BuildingKind.SOLAR_PLANT;
      case FUSION_REACTOR:
        return BuildingKind.FUSION_REACTOR;
      case ROBOTICS_FACTORY:
        return BuildingKind.ROBOTICS_FACTORY;
      case NANITE_FACTORY:
        return BuildingKind.NANITE_FACTORY;
      case SHIPYARD:
        return BuildingKind.SHIPYARD;
      case METAL_STORAGE:
        return BuildingKind.METAL_STORAGE;
      case CRYSTAL_STORAGE:
        return BuildingKind.CRYSTAL_STORAGE;
      case DEUTERIUM_TANK:
        return BuildingKind.DEUTERIUM_TANK;
      case RESEARCH_LAB:
        return BuildingKind.RESEARCH_LAB;
      case TERRAFORMER:
        return BuildingKind.TERRAFORMER;
      case ALLIANCE_DEPOT:
        return BuildingKind.ALLIANCE_DEPOT;
      case LUNAR_BASE:
        return BuildingKind.LUNAR_BASE;
      case SENSOR_PHALANX:
        return BuildingKind.SENSOR_PHALANX;
      case JUMP_GATE:
        return BuildingKind.JUMP_GATE;
      case MISSILE_SILO:
        return BuildingKind.MISSILE_SILO;
      default:
        throw new IllegalArgumentException("Illegal value for BuildingKindDto");
    }
  }

  static CombatResultDto convert(CombatResult entity) {
    switch (entity) {
      case WIN:
        return CombatResultDto.WIN;
      case DRAW:
        return CombatResultDto.DRAW;
      case LOSS:
        return CombatResultDto.LOSS;
      default:
        throw new IllegalArgumentException("Illegal value for CombatResult");
    }
  }

  public static CoordinatesDto convert(Coordinates entity) {
    return new CoordinatesDto(entity.getGalaxy(), entity.getSystem(), entity.getPosition(), convert(entity.getKind()));
  }

  static Coordinates convert(CoordinatesDto dto) {
    return new Coordinates(dto.getGalaxy(), dto.getSystem(), dto.getPosition(), convert(dto.getKind()));
  }

  static CoordinatesKindDto convert(CoordinatesKind entity) {
    switch (entity) {
      case PLANET:
        return CoordinatesKindDto.PLANET;
      case MOON:
        return CoordinatesKindDto.MOON;
      case DEBRIS_FIELD:
        return CoordinatesKindDto.DEBRIS_FIELD;
      default:
        throw new IllegalArgumentException("Illegal value for CoordinatesKind");
    }
  }

  static CoordinatesKind convert(CoordinatesKindDto dto) {
    switch (dto) {
      case PLANET:
        return CoordinatesKind.PLANET;
      case MOON:
        return CoordinatesKind.MOON;
      case DEBRIS_FIELD:
        return CoordinatesKind.DEBRIS_FIELD;
      default:
        throw new IllegalArgumentException("Illegal value for CoordinatesKindDto");
    }
  }

  static EspionageReportSortOrder convert(EspionageReportSortOrderDto dto) {
    switch (dto) {
      case AT:
        return EspionageReportSortOrder.AT;
      case ENEMY_NAME:
        return EspionageReportSortOrder.ENEMY_NAME;
      case COORDINATES:
        return EspionageReportSortOrder.COORDINATES;
      case ACTIVITY:
        return EspionageReportSortOrder.ACTIVITY;
      case RESOURCES:
        return EspionageReportSortOrder.RESOURCES;
      case FLEET:
        return EspionageReportSortOrder.FLEET;
      case DEFENSE:
        return EspionageReportSortOrder.DEFENSE;
      default:
        throw new IllegalArgumentException("Illegal value for EspionageReportSortOrderDto");
    }
  }

  static HarvestReportDto convert(HarvestReport entity) {
    return new HarvestReportDto(entity.getId(), entity.getAt(), Converter.convert(entity.getCoordinates()),
        entity.getNumRecyclers(), entity.getCapacity(), entity.getHarvestedMetal(), entity.getHarvestedCrystal(),
        entity.getRemainingMetal(), entity.getRemainingCrystal());
  }

  static HarvestReportSortOrder convert(HarvestReportSortOrderDto dto) {
    switch (dto) {
      case AT:
        return HarvestReportSortOrder.AT;
      case COORDINATES:
        return HarvestReportSortOrder.COORDINATES;
      case NUM_RECYCLERS:
        return HarvestReportSortOrder.NUM_RECYCLERS;
      case CAPACITY:
        return HarvestReportSortOrder.CAPACITY;
      case HARVESTED_RESOURCES:
        return HarvestReportSortOrder.HARVESTED_RESOURCES;
      case REMAINING_RESOURCES:
        return HarvestReportSortOrder.REMAINING_RESOURCES;
      default:
        throw new IllegalArgumentException("Illegal value for HarvestReportSortOrderDto");
    }
  }

  static MissionDto convert(Mission entity) {
    switch (entity) {
      case ATTACK:
        return MissionDto.ATTACK;
      case COLONIZATION:
        return MissionDto.COLONIZATION;
      case DEPLOYMENT:
        return MissionDto.DEPLOYMENT;
      case DESTROY:
        return MissionDto.DESTROY;
      case ESPIONAGE:
        return MissionDto.ESPIONAGE;
      case HARVEST:
        return MissionDto.HARVEST;
      case HOLD:
        return MissionDto.HOLD;
      case TRANSPORT:
        return MissionDto.TRANSPORT;
      case MISSILE_ATTACK:
        return MissionDto.MISSILE_ATTACK;
      default:
        throw new IllegalArgumentException("Illegal value for Mission");
    }
  }

  static Mission convert(MissionDto dto) {
    switch (dto) {
      case ATTACK:
        return Mission.ATTACK;
      case COLONIZATION:
        return Mission.COLONIZATION;
      case DEPLOYMENT:
        return Mission.DEPLOYMENT;
      case DESTROY:
        return Mission.DESTROY;
      case ESPIONAGE:
        return Mission.ESPIONAGE;
      case HARVEST:
        return Mission.HARVEST;
      case HOLD:
        return Mission.HOLD;
      case TRANSPORT:
        return Mission.TRANSPORT;
      case MISSILE_ATTACK:
        return Mission.MISSILE_ATTACK;
      default:
        throw new IllegalArgumentException("Illegal value for MissionDto");
    }
  }

  static OtherReportDto convert(OtherReport entity) {
    ResourcesDto resourcesDto = entity.getResources() != null ? Converter.convert(entity.getResources()) : null;
    return new OtherReportDto(entity.getId(), entity.getAt(), Converter.convert(entity.getKind()),
        Converter.convert(entity.getStartCoordinates()), Converter.convert(entity.getTargetCoordinates()), resourcesDto,
        entity.getParam());
  }

  private static OtherReportKindDto convert(OtherReportKind entity) {
    switch (entity) {
      case COLONIZATION:
        return OtherReportKindDto.COLONIZATION;
      case DEPLOYMENT:
        return OtherReportKindDto.DEPLOYMENT;
      case HOSTILE_ESPIONAGE:
        return OtherReportKindDto.HOSTILE_ESPIONAGE;
      case RETURN:
        return OtherReportKindDto.RETURN;
      case MISSILE_ATTACK:
        return OtherReportKindDto.MISSILE_ATTACK;
      default:
        throw new IllegalArgumentException("Illegal value for OtherReportKind");
    }
  }

  static ProductionFactorsDto convert(ProductionFactors entity) {
    return new ProductionFactorsDto(entity.getMetalMineFactor(), entity.getCrystalMineFactor(),
        entity.getDeuteriumSynthesizerFactor(), entity.getSolarPlantFactor(), entity.getFusionReactorFactor(),
        entity.getSolarSatellitesFactor());
  }

  static ProductionFactors convert(ProductionFactorsDto factors) {
    return new ProductionFactors(factors.getMetalMineFactor(), factors.getCrystalMineFactor(),
        factors.getDeuteriumSynthesizerFactor(), factors.getSolarPlantFactor(), factors.getFusionReactorFactor(),
        factors.getSolarSatellitesFactor());
  }

  static ResourcesDto convert(Resources entity) {
    return new ResourcesDto(entity.getMetal(), entity.getCrystal(), entity.getDeuterium());
  }

  static SimplifiedCombatReportSortOrder convert(SimplifiedCombatReportSortOrderDto dto) {
    switch (dto) {
      case AT:
        return SimplifiedCombatReportSortOrder.AT;
      case ENEMY_NAME:
        return SimplifiedCombatReportSortOrder.ENEMY_NAME;
      case COORDINATES:
        return SimplifiedCombatReportSortOrder.COORDINATES;
      case RESULT:
        return SimplifiedCombatReportSortOrder.RESULT;
      case ATTACKERS_LOSS:
        return SimplifiedCombatReportSortOrder.ATTACKERS_LOSS;
      case DEFENDERS_LOSS:
        return SimplifiedCombatReportSortOrder.DEFENDERS_LOSS;
      case PLUNDER:
        return SimplifiedCombatReportSortOrder.PLUNDER;
      case DEBRIS:
        return SimplifiedCombatReportSortOrder.DEBRIS;
      case MOON_CHANCE:
        return SimplifiedCombatReportSortOrder.MOON_CHANCE;
      default:
        throw new IllegalArgumentException("Illegal value for SimplifiedCombatReportSortOrderDto");
    }
  }

  static TechnologyKindDto convert(TechnologyKind entity) {
    switch (entity) {
      case ESPIONAGE_TECHNOLOGY:
        return TechnologyKindDto.ESPIONAGE_TECHNOLOGY;
      case COMPUTER_TECHNOLOGY:
        return TechnologyKindDto.COMPUTER_TECHNOLOGY;
      case WEAPONS_TECHNOLOGY:
        return TechnologyKindDto.WEAPONS_TECHNOLOGY;
      case SHIELDING_TECHNOLOGY:
        return TechnologyKindDto.SHIELDING_TECHNOLOGY;
      case ARMOR_TECHNOLOGY:
        return TechnologyKindDto.ARMOR_TECHNOLOGY;
      case ENERGY_TECHNOLOGY:
        return TechnologyKindDto.ENERGY_TECHNOLOGY;
      case HYPERSPACE_TECHNOLOGY:
        return TechnologyKindDto.HYPERSPACE_TECHNOLOGY;
      case COMBUSTION_DRIVE:
        return TechnologyKindDto.COMBUSTION_DRIVE;
      case IMPULSE_DRIVE:
        return TechnologyKindDto.IMPULSE_DRIVE;
      case HYPERSPACE_DRIVE:
        return TechnologyKindDto.HYPERSPACE_DRIVE;
      case LASER_TECHNOLOGY:
        return TechnologyKindDto.LASER_TECHNOLOGY;
      case ION_TECHNOLOGY:
        return TechnologyKindDto.ION_TECHNOLOGY;
      case PLASMA_TECHNOLOGY:
        return TechnologyKindDto.PLASMA_TECHNOLOGY;
      case INTERGALACTIC_RESEARCH_NETWORK:
        return TechnologyKindDto.INTERGALACTIC_RESEARCH_NETWORK;
      case ASTROPHYSICS:
        return TechnologyKindDto.ASTROPHYSICS;
      case GRAVITON_TECHNOLOGY:
        return TechnologyKindDto.GRAVITON_TECHNOLOGY;
      default:
        throw new IllegalArgumentException("Illegal value for TechnologyKind");
    }
  }

  static TechnologyKind convert(TechnologyKindDto dto) {
    switch (dto) {
      case ESPIONAGE_TECHNOLOGY:
        return TechnologyKind.ESPIONAGE_TECHNOLOGY;
      case COMPUTER_TECHNOLOGY:
        return TechnologyKind.COMPUTER_TECHNOLOGY;
      case WEAPONS_TECHNOLOGY:
        return TechnologyKind.WEAPONS_TECHNOLOGY;
      case SHIELDING_TECHNOLOGY:
        return TechnologyKind.SHIELDING_TECHNOLOGY;
      case ARMOR_TECHNOLOGY:
        return TechnologyKind.ARMOR_TECHNOLOGY;
      case ENERGY_TECHNOLOGY:
        return TechnologyKind.ENERGY_TECHNOLOGY;
      case HYPERSPACE_TECHNOLOGY:
        return TechnologyKind.HYPERSPACE_TECHNOLOGY;
      case COMBUSTION_DRIVE:
        return TechnologyKind.COMBUSTION_DRIVE;
      case IMPULSE_DRIVE:
        return TechnologyKind.IMPULSE_DRIVE;
      case HYPERSPACE_DRIVE:
        return TechnologyKind.HYPERSPACE_DRIVE;
      case LASER_TECHNOLOGY:
        return TechnologyKind.LASER_TECHNOLOGY;
      case ION_TECHNOLOGY:
        return TechnologyKind.ION_TECHNOLOGY;
      case PLASMA_TECHNOLOGY:
        return TechnologyKind.PLASMA_TECHNOLOGY;
      case INTERGALACTIC_RESEARCH_NETWORK:
        return TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK;
      case ASTROPHYSICS:
        return TechnologyKind.ASTROPHYSICS;
      case GRAVITON_TECHNOLOGY:
        return TechnologyKind.GRAVITON_TECHNOLOGY;
      default:
        throw new IllegalArgumentException("Illegal value for TechnologyKindDto");
    }
  }

  private static TransportKindDto convert(TransportKind entity) {
    switch (entity) {
      case INCOMING:
        return TransportKindDto.INCOMING;
      case OUTGOING:
        return TransportKindDto.OUTGOING;
      case OWN:
        return TransportKindDto.OWN;
      default:
        throw new IllegalArgumentException("Illegal value for TransportKind");
    }
  }

  static TransportReportDto convert(TransportReport entity) {
    return new TransportReportDto(entity.getId(), entity.getAt(), Converter.convert(entity.getKind()),
        entity.getPartnerId(), entity.getPartnerName(), Converter.convert(entity.getStartCoordinates()),
        Converter.convert(entity.getTargetCoordinates()), Converter.convert(entity.getResources()));
  }

  static TransportReportSortOrder convert(TransportReportSortOrderDto dto) {
    switch (dto) {
      case AT:
        return TransportReportSortOrder.AT;
      case KIND:
        return TransportReportSortOrder.KIND;
      case PARTNER_NAME:
        return TransportReportSortOrder.PARTNER_NAME;
      case START_COORDINATES:
        return TransportReportSortOrder.START_COORDINATES;
      case TARGET_COORDINATES:
        return TransportReportSortOrder.TARGET_COORDINATES;
      case RESOURCES:
        return TransportReportSortOrder.RESOURCES;
      default:
        throw new IllegalArgumentException("Illegal value for TransportReportSortOrderDto");
    }
  }

  static UnitKindDto convert(UnitKind entity) {
    switch (entity) {
      case SMALL_CARGO:
        return UnitKindDto.SMALL_CARGO;
      case LARGE_CARGO:
        return UnitKindDto.LARGE_CARGO;
      case LITTLE_FIGHTER:
        return UnitKindDto.LITTLE_FIGHTER;
      case HEAVY_FIGHTER:
        return UnitKindDto.HEAVY_FIGHTER;
      case CRUISER:
        return UnitKindDto.CRUISER;
      case BATTLESHIP:
        return UnitKindDto.BATTLESHIP;
      case COLONY_SHIP:
        return UnitKindDto.COLONY_SHIP;
      case RECYCLER:
        return UnitKindDto.RECYCLER;
      case ESPIONAGE_PROBE:
        return UnitKindDto.ESPIONAGE_PROBE;
      case BOMBER:
        return UnitKindDto.BOMBER;
      case SOLAR_SATELLITE:
        return UnitKindDto.SOLAR_SATELLITE;
      case DESTROYER:
        return UnitKindDto.DESTROYER;
      case DEATH_STAR:
        return UnitKindDto.DEATH_STAR;
      case ROCKET_LAUNCHER:
        return UnitKindDto.ROCKET_LAUNCHER;
      case LIGHT_LASER:
        return UnitKindDto.LIGHT_LASER;
      case HEAVY_LASER:
        return UnitKindDto.HEAVY_LASER;
      case GAIUS_CANNON:
        return UnitKindDto.GAIUS_CANNON;
      case ION_CANNON:
        return UnitKindDto.ION_CANNON;
      case PLASMA_TURRET:
        return UnitKindDto.PLASMA_TURRET;
      case SMALL_SHIELD_DOME:
        return UnitKindDto.SMALL_SHIELD_DOME;
      case LARGE_SHIELD_DOME:
        return UnitKindDto.LARGE_SHIELD_DOME;
      case ANTI_BALLISTIC_MISSILE:
        return UnitKindDto.ANTI_BALLISTIC_MISSILE;
      case INTERPLANETARY_MISSILE:
        return UnitKindDto.INTERPLANETARY_MISSILE;
      default:
        throw new IllegalArgumentException("Illegal value for UnitKind");
    }
  }

  static UnitKind convert(UnitKindDto dto) {
    switch (dto) {
      case SMALL_CARGO:
        return UnitKind.SMALL_CARGO;
      case LARGE_CARGO:
        return UnitKind.LARGE_CARGO;
      case LITTLE_FIGHTER:
        return UnitKind.LITTLE_FIGHTER;
      case HEAVY_FIGHTER:
        return UnitKind.HEAVY_FIGHTER;
      case CRUISER:
        return UnitKind.CRUISER;
      case BATTLESHIP:
        return UnitKind.BATTLESHIP;
      case COLONY_SHIP:
        return UnitKind.COLONY_SHIP;
      case RECYCLER:
        return UnitKind.RECYCLER;
      case ESPIONAGE_PROBE:
        return UnitKind.ESPIONAGE_PROBE;
      case BOMBER:
        return UnitKind.BOMBER;
      case SOLAR_SATELLITE:
        return UnitKind.SOLAR_SATELLITE;
      case DESTROYER:
        return UnitKind.DESTROYER;
      case DEATH_STAR:
        return UnitKind.DEATH_STAR;
      case ROCKET_LAUNCHER:
        return UnitKind.ROCKET_LAUNCHER;
      case LIGHT_LASER:
        return UnitKind.LIGHT_LASER;
      case HEAVY_LASER:
        return UnitKind.HEAVY_LASER;
      case GAIUS_CANNON:
        return UnitKind.GAIUS_CANNON;
      case ION_CANNON:
        return UnitKind.ION_CANNON;
      case PLASMA_TURRET:
        return UnitKind.PLASMA_TURRET;
      case SMALL_SHIELD_DOME:
        return UnitKind.SMALL_SHIELD_DOME;
      case LARGE_SHIELD_DOME:
        return UnitKind.LARGE_SHIELD_DOME;
      case ANTI_BALLISTIC_MISSILE:
        return UnitKind.ANTI_BALLISTIC_MISSILE;
      case INTERPLANETARY_MISSILE:
        return UnitKind.INTERPLANETARY_MISSILE;
      default:
        throw new IllegalArgumentException("Illegal value for UnitKindDto");
    }
  }

  static Map<BuildingKindDto, Integer> convertBuildingsRequirements(
      Map<BuildingKind, Integer> buildingsRequirements) {
    Map<BuildingKindDto, Integer> map = new EnumMap<>(BuildingKindDto.class);
    for (Map.Entry<BuildingKind, Integer> entry : buildingsRequirements.entrySet()) {
      map.put(convert(entry.getKey()), entry.getValue());
    }
    return map;
  }

  static Map<TechnologyKindDto, Integer> convertTechnologiesRequirements(
      Map<TechnologyKind, Integer> technologiesRequirements) {
    Map<TechnologyKindDto, Integer> map = new EnumMap<>(TechnologyKindDto.class);
    for (Map.Entry<TechnologyKind, Integer> entry : technologiesRequirements.entrySet()) {
      map.put(convert(entry.getKey()), entry.getValue());
    }
    return map;
  }
}
