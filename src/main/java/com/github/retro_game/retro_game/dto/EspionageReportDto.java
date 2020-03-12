package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.Map;

public class EspionageReportDto {
  private final Date at;
  private final Long enemyId;
  private final String enemyName;
  private final CoordinatesDto coordinates;
  private final int diameter;
  private final int activity;
  private final double counterChance;
  private final ResourcesDto resources;
  private final Map<UnitKindDto, Integer> fleet;
  private final Map<UnitKindDto, Integer> defense;
  private final Map<BuildingKindDto, Integer> buildings;
  private final Map<TechnologyKindDto, Integer> technologies;

  public EspionageReportDto(Date at, @Nullable Long enemyId, String enemyName, CoordinatesDto coordinates, int diameter,
                            int activity, double counterChance, ResourcesDto resources,
                            @Nullable Map<UnitKindDto, Integer> fleet, @Nullable Map<UnitKindDto, Integer> defense,
                            @Nullable Map<BuildingKindDto, Integer> buildings,
                            @Nullable Map<TechnologyKindDto, Integer> technologies) {
    this.at = at;
    this.enemyId = enemyId;
    this.enemyName = enemyName;
    this.coordinates = coordinates;
    this.diameter = diameter;
    this.activity = activity;
    this.counterChance = counterChance;
    this.resources = resources;
    this.fleet = fleet;
    this.defense = defense;
    this.buildings = buildings;
    this.technologies = technologies;
  }

  public Date getAt() {
    return at;
  }

  public Long getEnemyId() {
    return enemyId;
  }

  public String getEnemyName() {
    return enemyName;
  }

  public CoordinatesDto getCoordinates() {
    return coordinates;
  }

  public int getDiameter() {
    return diameter;
  }

  public int getActivity() {
    return activity;
  }

  public double getCounterChance() {
    return counterChance;
  }

  public ResourcesDto getResources() {
    return resources;
  }

  public Map<UnitKindDto, Integer> getFleet() {
    return fleet;
  }

  public Map<UnitKindDto, Integer> getDefense() {
    return defense;
  }

  public Map<BuildingKindDto, Integer> getBuildings() {
    return buildings;
  }

  public Map<TechnologyKindDto, Integer> getTechnologies() {
    return technologies;
  }
}
