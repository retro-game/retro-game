package com.github.retro_game.retro_game.dto;

import java.util.Date;
import java.util.List;

public class CombatReportDto {
  private final Date at;
  private final List<CombatReportCombatantDto> attackers;
  private final List<CombatReportCombatantDto> defenders;
  private final List<CombatReportRoundDto> rounds;
  private final BattleResultDto result;
  private final long attackersLoss;
  private final long defendersLoss;
  private final ResourcesDto plunder;
  private final long debrisMetal;
  private final long debrisCrystal;
  private final double moonChance;
  private final boolean moonGiven;
  private final int seed;
  private final long executionTime;

  public CombatReportDto(Date at, List<CombatReportCombatantDto> attackers, List<CombatReportCombatantDto> defenders,
                         List<CombatReportRoundDto> rounds, BattleResultDto result, long attackersLoss,
                         long defendersLoss, ResourcesDto plunder, long debrisMetal, long debrisCrystal,
                         double moonChance, boolean moonGiven, int seed, long executionTime) {
    this.at = at;
    this.attackers = attackers;
    this.defenders = defenders;
    this.rounds = rounds;
    this.result = result;
    this.attackersLoss = attackersLoss;
    this.defendersLoss = defendersLoss;
    this.plunder = plunder;
    this.debrisMetal = debrisMetal;
    this.debrisCrystal = debrisCrystal;
    this.moonChance = moonChance;
    this.moonGiven = moonGiven;
    this.seed = seed;
    this.executionTime = executionTime;
  }

  public Date getAt() {
    return at;
  }

  public List<CombatReportCombatantDto> getAttackers() {
    return attackers;
  }

  public List<CombatReportCombatantDto> getDefenders() {
    return defenders;
  }

  public List<CombatReportRoundDto> getRounds() {
    return rounds;
  }

  public BattleResultDto getResult() {
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

  public long getDebrisMetal() {
    return debrisMetal;
  }

  public long getDebrisCrystal() {
    return debrisCrystal;
  }

  public double getMoonChance() {
    return moonChance;
  }

  public boolean isMoonGiven() {
    return moonGiven;
  }

  public int getSeed() {
    return seed;
  }

  public long getExecutionTime() {
    return executionTime;
  }
}
