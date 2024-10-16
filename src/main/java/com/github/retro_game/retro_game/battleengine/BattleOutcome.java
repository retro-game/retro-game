package com.github.retro_game.retro_game.battleengine;

import java.util.List;

public record BattleOutcome(int numRounds, List<CombatantOutcome> attackersOutcomes,
                            List<CombatantOutcome> defendersOutcomes) {
}
