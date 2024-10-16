package com.github.retro_game.retro_game.dto;

import java.util.EnumMap;

public record CombatReportCombatantDto(long userId, CoordinatesDto coordinates, int weaponsTechnology,
                                       int shieldingTechnology, int armorTechnology,
                                       EnumMap<UnitKindDto, CombatReportUnitGroupDto> unitGroups) {
}
