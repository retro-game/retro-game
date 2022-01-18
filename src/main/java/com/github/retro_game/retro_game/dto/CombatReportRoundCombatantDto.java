package com.github.retro_game.retro_game.dto;

import java.util.EnumMap;

public record CombatReportRoundCombatantDto(long userId,
                                            EnumMap<UnitKindDto, CombatReportRoundUnitGroupDto> unitGroups) {
}
