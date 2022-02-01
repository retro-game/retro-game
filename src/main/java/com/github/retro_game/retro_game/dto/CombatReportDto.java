package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

public record CombatReportDto(Date at, List<CombatReportCombatantDto> attackers,
                              List<CombatReportCombatantDto> defenders, List<CombatReportRoundDto> rounds,
                              BattleResultDto result, long attackersLoss, long defendersLoss, ResourcesDto plunder,
                              long debrisMetal, long debrisCrystal, double moonChance, boolean moonGiven,
                              @Nullable MoonDestructionResultDto moonDestructionResult, int seed, long executionTime) {
}
