package com.github.retro_game.retro_game.dto;

import java.util.ArrayList;

public record CombatReportRoundDto(ArrayList<CombatReportRoundCombatantDto> attackers,
                                   ArrayList<CombatReportRoundCombatantDto> defenders) {
}
