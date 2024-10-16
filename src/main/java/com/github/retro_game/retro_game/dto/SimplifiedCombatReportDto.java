package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.UUID;

public record SimplifiedCombatReportDto(long id, Date at, Long enemyId, String enemyName, CoordinatesDto coordinates,
                                        CombatResultDto result, long attackersLoss, long defendersLoss,
                                        ResourcesDto plunder, double debrisMetal, double debrisCrystal,
                                        double moonChance, boolean moonGiven, @Nullable UUID combatReportId) {
}
