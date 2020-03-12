package com.github.retro_game.retro_game.battleengine;

public class CombatantOutcome {
  private static final int TIMES_FIRED_OFFSET = 0;
  private static final int TIMES_WAS_SHOT_OFFSET = 1;
  private static final int SHIELD_DAMAGE_DEALT_OFFSET = 2;
  private static final int HULL_DAMAGE_DEALT_OFFSET = 3;
  private static final int SHIELD_DAMAGE_TAKEN_OFFSET = 4;
  private static final int HULL_DAMAGE_TAKEN_OFFSET = 5;
  private static final int NUM_REMAINING_UNITS_OFFSET = 6;
  private static final int NUM_STRUCT_ELEMENTS = 7;
  private final int numKinds;
  private final long[] unitGroupStats;

  public CombatantOutcome(int numKinds, long[] unitGroupStats) {
    this.numKinds = numKinds;
    this.unitGroupStats = unitGroupStats;
  }

  // Checks whether the unit group was active in the round.
  public boolean wasActive(int round, int kind) {
    return getTimesFired(round, kind) != 0;
  }

  public long getTimesFired(int round, int kind) {
    return get(round, kind, TIMES_FIRED_OFFSET);
  }

  public long getTimesWasShot(int round, int kind) {
    return get(round, kind, TIMES_WAS_SHOT_OFFSET);
  }

  public long getShieldDamageDealt(int round, int kind) {
    return get(round, kind, SHIELD_DAMAGE_DEALT_OFFSET);
  }

  public long getHullDamageDealt(int round, int kind) {
    return get(round, kind, HULL_DAMAGE_DEALT_OFFSET);
  }

  public long getShieldDamageTaken(int round, int kind) {
    return get(round, kind, SHIELD_DAMAGE_TAKEN_OFFSET);
  }

  public long getHullDamageTaken(int round, int kind) {
    return get(round, kind, HULL_DAMAGE_TAKEN_OFFSET);
  }

  public long getNumRemainingUnits(int round, int kind) {
    return get(round, kind, NUM_REMAINING_UNITS_OFFSET);
  }

  private long get(int round, int kind, int offset) {
    assert kind >= 0 && kind < numKinds;
    int index = (round * numKinds + kind) * NUM_STRUCT_ELEMENTS + offset;
    return unitGroupStats[index];
  }

  public int getNumKinds() {
    return numKinds;
  }
}
