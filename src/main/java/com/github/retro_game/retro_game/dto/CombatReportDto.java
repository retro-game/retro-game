package com.github.retro_game.retro_game.dto;

import java.util.ArrayList;
import java.util.Date;

public record CombatReportDto(Date at, ArrayList<CombatReportCombatantDto> attackers,
                              ArrayList<CombatReportCombatantDto> defenders, ArrayList<CombatReportRoundDto> rounds,
                              BattleResultDto result, long attackersLoss, long defendersLoss, ResourcesDto plunder,
                              long debrisMetal, long debrisCrystal, double moonChance, boolean moonGiven, int seed,
                              long executionTime) {
}
