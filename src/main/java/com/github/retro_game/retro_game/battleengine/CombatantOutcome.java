package com.github.retro_game.retro_game.battleengine;

import com.github.retro_game.retro_game.entity.UnitKind;

import java.util.EnumMap;
import java.util.List;

public final class CombatantOutcome {
  // A mapping: round -> unit groups stats.
  private final List<EnumMap<UnitKind, UnitGroupStats>> unitGroupsStats;

  public CombatantOutcome(List<EnumMap<UnitKind, UnitGroupStats>> unitGroupsStats) {
    this.unitGroupsStats = unitGroupsStats;
  }

  public EnumMap<UnitKind, UnitGroupStats> getNthRoundUnitGroupsStats(int round) {
    return unitGroupsStats.get(round);
  }

  public List<EnumMap<UnitKind, UnitGroupStats>> getUnitGroupsStats() {
    return unitGroupsStats;
  }
}
