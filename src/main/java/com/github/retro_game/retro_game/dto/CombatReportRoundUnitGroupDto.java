package com.github.retro_game.retro_game.dto;

public class CombatReportRoundUnitGroupDto {
  private final long numRemainingUnits;
  private final long timesFired;
  private final long timesWasShot;
  private final long shieldDamageDealt;
  private final long hullDamageDealt;
  private final long shieldDamageTaken;
  private final long hullDamageTaken;

  public CombatReportRoundUnitGroupDto(long numRemainingUnits, long timesFired, long timesWasShot,
                                       long shieldDamageDealt, long hullDamageDealt, long shieldDamageTaken,
                                       long hullDamageTaken) {
    this.numRemainingUnits = numRemainingUnits;
    this.timesFired = timesFired;
    this.timesWasShot = timesWasShot;
    this.shieldDamageDealt = shieldDamageDealt;
    this.hullDamageDealt = hullDamageDealt;
    this.shieldDamageTaken = shieldDamageTaken;
    this.hullDamageTaken = hullDamageTaken;
  }

  public long getNumRemainingUnits() {
    return numRemainingUnits;
  }

  public long getTimesFired() {
    return timesFired;
  }

  public long getTimesWasShot() {
    return timesWasShot;
  }

  public long getShieldDamageDealt() {
    return shieldDamageDealt;
  }

  public long getHullDamageDealt() {
    return hullDamageDealt;
  }

  public long getShieldDamageTaken() {
    return shieldDamageTaken;
  }

  public long getHullDamageTaken() {
    return hullDamageTaken;
  }
}
