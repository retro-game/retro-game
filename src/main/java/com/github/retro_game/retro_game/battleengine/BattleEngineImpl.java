package com.github.retro_game.retro_game.battleengine;

import com.github.retro_game.retro_game.entity.UnitKind;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Component
public class BattleEngineImpl implements BattleEngine {
  private final BattleEngineStrategy battleEngineStrategy;

  public BattleEngineImpl(BattleEngineStrategy battleEngineStrategy) {
    this.battleEngineStrategy = battleEngineStrategy;
  }

  @Override
  public BattleOutcome fight(List<Combatant> attackers, List<Combatant> defenders, int seed) {
    var numAttackersUnits = totalUnits(attackers);
    var numDefendersUnits = totalUnits(defenders);
    if (numAttackersUnits == 0 || numDefendersUnits == 0) {
      var attackersOutcomes = makeOutcomes(attackers);
      var defendersOutcomes = makeOutcomes(defenders);
      return new BattleOutcome(1, attackersOutcomes, defendersOutcomes);
    }

    return battleEngineStrategy.fight(attackers, defenders, seed);
  }

  private static long totalUnits(List<Combatant> combatants) {
    return combatants.stream().mapToLong(c -> c.unitGroups().values().stream().mapToLong(Long::longValue).sum()).sum();
  }

  private static List<CombatantOutcome> makeOutcomes(List<Combatant> combatants) {
    var outcomes = new ArrayList<CombatantOutcome>(combatants.size());
    for (var combatant : combatants) {
      var unitGroups = combatant.unitGroups();
      var roundStats = new EnumMap<UnitKind, UnitGroupStats>(UnitKind.class);
      for (var kind : UnitKind.values()) {
        var count = unitGroups.getOrDefault(kind, 0L);
        assert count >= 0L;
        roundStats.put(kind, new UnitGroupStats(count, 0, 0, 0, 0, 0, 0));
      }
      var combatantStats = List.of(roundStats);
      var outcome = new CombatantOutcome(combatantStats);
      outcomes.add(outcome);
    }
    return outcomes;
  }
}
