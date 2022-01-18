package com.github.retro_game.retro_game.dto;

public record CombatReportRoundUnitGroupDto(long numRemainingUnits, long timesFired, long timesWasShot,
                                            long shieldDamageDealt, long hullDamageDealt, long shieldDamageTaken,
                                            long hullDamageTaken) {
}
