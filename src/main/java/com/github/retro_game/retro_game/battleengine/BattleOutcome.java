package com.github.retro_game.retro_game.battleengine;

import java.util.List;

public final class BattleOutcome {
  private final int numRounds;
  private final List<CombatantOutcome> attackersOutcomes;
  private final List<CombatantOutcome> defendersOutcomes;

  public BattleOutcome(int numRounds, List<CombatantOutcome> attackersOutcomes,
                       List<CombatantOutcome> defendersOutcomes) {
    assert attackersOutcomes.stream().allMatch(o -> o.getUnitGroupsStats().size() == numRounds);
    assert defendersOutcomes.stream().allMatch(o -> o.getUnitGroupsStats().size() == numRounds);
    this.numRounds = numRounds;
    this.attackersOutcomes = attackersOutcomes;
    this.defendersOutcomes = defendersOutcomes;
  }

  public int getNumRounds() {
    return numRounds;
  }

  public List<CombatantOutcome> getAttackersOutcomes() {
    return attackersOutcomes;
  }

  public List<CombatantOutcome> getDefendersOutcomes() {
    return defendersOutcomes;
  }
}
