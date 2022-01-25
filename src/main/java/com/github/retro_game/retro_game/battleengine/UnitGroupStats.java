package com.github.retro_game.retro_game.battleengine;

// Statistics of a unit group (e.g., all battleships) after a battle round.
public record UnitGroupStats(long numRemainingUnits, long timesFired, long timesWasShot, float shieldDamageDealt,
                             float hullDamageDealt, float shieldDamageTaken, float hullDamageTaken) {
}
