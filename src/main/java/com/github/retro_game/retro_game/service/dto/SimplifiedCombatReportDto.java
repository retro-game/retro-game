package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class SimplifiedCombatReportDto {
  private final long id;
  private final Date at;
  private final Long enemyId;
  private final String enemyName;
  private final CoordinatesDto coordinates;
  private final CombatResultDto result;
  private final long attackersLoss;
  private final long defendersLoss;
  private final ResourcesDto plunder;
  private final double debrisMetal;
  private final double debrisCrystal;
  private final double moonChance;
  private final boolean moonGiven;
  private final Long combatReportId;
  private final String token;

  public SimplifiedCombatReportDto(long id, Date at, Long enemyId, String enemyName, CoordinatesDto coordinates,
                                   CombatResultDto result, long attackersLoss, long defendersLoss, ResourcesDto plunder,
                                   double debrisMetal, double debrisCrystal, double moonChance, boolean moonGiven,
                                   @Nullable Long combatReportId, @Nullable String token) {
    this.id = id;
    this.at = at;
    this.enemyId = enemyId;
    this.enemyName = enemyName;
    this.coordinates = coordinates;
    this.result = result;
    this.attackersLoss = attackersLoss;
    this.defendersLoss = defendersLoss;
    this.plunder = plunder;
    this.debrisMetal = debrisMetal;
    this.debrisCrystal = debrisCrystal;
    this.moonChance = moonChance;
    this.moonGiven = moonGiven;
    this.combatReportId = combatReportId;
    this.token = token;
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

  public CombatResultDto getResult() {
    return result;
  }

  public long getAttackersLoss() {
    return attackersLoss;
  }

  public long getDefendersLoss() {
    return defendersLoss;
  }

  public ResourcesDto getPlunder() {
    return plunder;
  }

  public double getDebrisMetal() {
    return debrisMetal;
  }

  public double getDebrisCrystal() {
    return debrisCrystal;
  }

  public double getMoonChance() {
    return moonChance;
  }

  public boolean isMoonGiven() {
    return moonGiven;
  }

  public Long getCombatReportId() {
    return combatReportId;
  }

  public String getToken() {
    return token;
  }
}

