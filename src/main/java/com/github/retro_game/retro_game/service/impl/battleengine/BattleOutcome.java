package com.github.retro_game.retro_game.service.impl.battleengine;

public class BattleOutcome {
  private final int numRounds;
  private final CombatantOutcome[] attackersOutcomes;
  private final CombatantOutcome[] defendersOutcomes;

  public BattleOutcome(int numRounds, CombatantOutcome[] attackersOutcomes, CombatantOutcome[] defendersOutcomes) {
    this.numRounds = numRounds;
    this.attackersOutcomes = attackersOutcomes;
    this.defendersOutcomes = defendersOutcomes;
  }

  public int getNumRounds() {
    return numRounds;
  }

  public CombatantOutcome[] getAttackersOutcomes() {
    return attackersOutcomes;
  }

  public CombatantOutcome[] getDefendersOutcomes() {
    return defendersOutcomes;
  }
}
