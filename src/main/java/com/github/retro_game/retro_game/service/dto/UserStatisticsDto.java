package com.github.retro_game.retro_game.service.dto;

public class UserStatisticsDto {
  private final PointsAndRankPairDto overall;
  private final PointsAndRankPairDto buildings;
  private final PointsAndRankPairDto technologies;
  private final PointsAndRankPairDto fleet;
  private final PointsAndRankPairDto defense;

  public UserStatisticsDto(PointsAndRankPairDto overall, PointsAndRankPairDto buildings,
                           PointsAndRankPairDto technologies, PointsAndRankPairDto fleet,
                           PointsAndRankPairDto defense) {
    this.overall = overall;
    this.buildings = buildings;
    this.technologies = technologies;
    this.fleet = fleet;
    this.defense = defense;
  }

  public PointsAndRankPairDto getOverall() {
    return overall;
  }

  public PointsAndRankPairDto getBuildings() {
    return buildings;
  }

  public PointsAndRankPairDto getTechnologies() {
    return technologies;
  }

  public PointsAndRankPairDto getFleet() {
    return fleet;
  }

  public PointsAndRankPairDto getDefense() {
    return defense;
  }
}
