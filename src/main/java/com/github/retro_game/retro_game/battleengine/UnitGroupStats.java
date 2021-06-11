package com.github.retro_game.retro_game.battleengine;

import java.util.Objects;

// Statistics of a unit group (e.g., all battleships) after a battle round.
public final class UnitGroupStats {
  private final long numRemainingUnits;
  private final long timesFired;
  private final long timesWasShot;
  private final float shieldDamageDealt;
  private final float hullDamageDealt;
  private final float shieldDamageTaken;
  private final float hullDamageTaken;

  public UnitGroupStats(long numRemainingUnits, long timesFired, long timesWasShot, float shieldDamageDealt,
                        float hullDamageDealt, float shieldDamageTaken, float hullDamageTaken) {
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

  public float getShieldDamageDealt() {
    return shieldDamageDealt;
  }

  public float getHullDamageDealt() {
    return hullDamageDealt;
  }

  public float getShieldDamageTaken() {
    return shieldDamageTaken;
  }

  public float getHullDamageTaken() {
    return hullDamageTaken;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UnitGroupStats that = (UnitGroupStats) o;
    return numRemainingUnits == that.numRemainingUnits &&
        timesFired == that.timesFired &&
        timesWasShot == that.timesWasShot &&
        Float.compare(that.shieldDamageDealt, shieldDamageDealt) == 0 &&
        Float.compare(that.hullDamageDealt, hullDamageDealt) == 0 &&
        Float.compare(that.shieldDamageTaken, shieldDamageTaken) == 0 &&
        Float.compare(that.hullDamageTaken, hullDamageTaken) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(numRemainingUnits, timesFired, timesWasShot, shieldDamageDealt, hullDamageDealt,
        shieldDamageTaken, hullDamageTaken);
  }

  @Override
  public String toString() {
    return "UnitGroupStats{" +
        "numRemainingUnits=" + numRemainingUnits +
        ", timesFired=" + timesFired +
        ", timesWasShot=" + timesWasShot +
        ", shieldDamageDealt=" + shieldDamageDealt +
        ", hullDamageDealt=" + hullDamageDealt +
        ", shieldDamageTaken=" + shieldDamageTaken +
        ", hullDamageTaken=" + hullDamageTaken +
        '}';
  }
}
