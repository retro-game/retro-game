package com.github.retro_game.retro_game.service.dto;

public class StatisticsDistributionDto {
  private final int buildings;
  private final int technologies;
  private final int fleet;
  private final int defense;

  public StatisticsDistributionDto(int buildings, int technologies, int fleet, int defense) {
    this.buildings = buildings;
    this.technologies = technologies;
    this.fleet = fleet;
    this.defense = defense;
  }

  public int getBuildings() {
    return buildings;
  }

  public int getTechnologies() {
    return technologies;
  }

  public int getFleet() {
    return fleet;
  }

  public int getDefense() {
    return defense;
  }
}
