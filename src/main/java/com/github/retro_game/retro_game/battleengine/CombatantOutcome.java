package com.github.retro_game.retro_game.battleengine;

import com.github.retro_game.retro_game.entity.UnitKind;

import java.util.EnumMap;
import java.util.List;

public record CombatantOutcome(List<EnumMap<UnitKind, UnitGroupStats>> unitGroupsStats) {
  public EnumMap<UnitKind, UnitGroupStats> getNthRoundUnitGroupsStats(int round) {
    return unitGroupsStats.get(round);
  }
}
