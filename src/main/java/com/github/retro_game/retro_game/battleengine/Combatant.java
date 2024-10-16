package com.github.retro_game.retro_game.battleengine;

import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.UnitKind;

import java.util.EnumMap;

public record Combatant(long userId, Coordinates coordinates, int weaponsTechnology, int shieldingTechnology,
                        int armorTechnology, EnumMap<UnitKind, Long> unitGroups) {
}
