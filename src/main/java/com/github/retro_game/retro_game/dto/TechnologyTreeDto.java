package com.github.retro_game.retro_game.dto;

import java.util.Map;

public class TechnologyTreeDto {
  private final Map<BuildingKindDto, RequirementsDto> buildings;
  private final Map<TechnologyKindDto, RequirementsDto> technologies;
  private final Map<UnitKindDto, RequirementsDto> fleet;
  private final Map<UnitKindDto, RequirementsDto> defense;

  public TechnologyTreeDto(Map<BuildingKindDto, RequirementsDto> buildings,
                           Map<TechnologyKindDto, RequirementsDto> technologies,
                           Map<UnitKindDto, RequirementsDto> fleet, Map<UnitKindDto, RequirementsDto> defense) {
    this.buildings = buildings;
    this.technologies = technologies;
    this.fleet = fleet;
    this.defense = defense;
  }

  public Map<BuildingKindDto, RequirementsDto> getBuildings() {
    return buildings;
  }

  public Map<TechnologyKindDto, RequirementsDto> getTechnologies() {
    return technologies;
  }

  public Map<UnitKindDto, RequirementsDto> getFleet() {
    return fleet;
  }

  public Map<UnitKindDto, RequirementsDto> getDefense() {
    return defense;
  }
}
