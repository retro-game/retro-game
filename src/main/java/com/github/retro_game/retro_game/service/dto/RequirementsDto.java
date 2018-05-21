package com.github.retro_game.retro_game.service.dto;

import java.util.Map;

public class RequirementsDto {
  private final Map<BuildingKindDto, Integer> buildings;
  private final Map<TechnologyKindDto, Integer> technologies;

  public RequirementsDto(Map<BuildingKindDto, Integer> buildings, Map<TechnologyKindDto, Integer> technologies) {
    this.buildings = buildings;
    this.technologies = technologies;
  }

  public Map<BuildingKindDto, Integer> getBuildings() {
    return buildings;
  }

  public Map<TechnologyKindDto, Integer> getTechnologies() {
    return technologies;
  }
}
