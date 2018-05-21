package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class SimplifiedEspionageReportDto {
  private final long id;
  private final Date at;
  private final Long enemyId;
  private final String enemyName;
  private final CoordinatesDto coordinates;
  private final int activity;
  private final ResourcesDto resources;
  private final Long fleet;
  private final Long defense;
  private final String token;
  private final int neededSmallCargoes;
  private final int neededLargeCargoes;

  public SimplifiedEspionageReportDto(long id, Date at, @Nullable Long enemyId, String enemyName,
                                      CoordinatesDto coordinates, int activity, ResourcesDto resources,
                                      @Nullable Long fleet, @Nullable Long defense, String token,
                                      int neededSmallCargoes, int neededLargeCargoes) {
    this.id = id;
    this.at = at;
    this.enemyId = enemyId;
    this.enemyName = enemyName;
    this.coordinates = coordinates;
    this.activity = activity;
    this.resources = resources;
    this.fleet = fleet;
    this.defense = defense;
    this.token = token;
    this.neededSmallCargoes = neededSmallCargoes;
    this.neededLargeCargoes = neededLargeCargoes;
  }

  public long getId() {
    return id;
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

  public int getActivity() {
    return activity;
  }

  public ResourcesDto getResources() {
    return resources;
  }

  public Long getFleet() {
    return fleet;
  }

  public Long getDefense() {
    return defense;
  }

  public String getToken() {
    return token;
  }

  public int getNeededSmallCargoes() {
    return neededSmallCargoes;
  }

  public int getNeededLargeCargoes() {
    return neededLargeCargoes;
  }
}
