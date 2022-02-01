package com.github.retro_game.retro_game.battleengine;

import java.util.List;

public interface BattleEngineStrategy {
  BattleOutcome fight(List<Combatant> attackers, List<Combatant> defenders, int seed);
}
