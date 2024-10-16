package com.github.retro_game.retro_game.dto;

import java.util.List;

public record UnitsAndQueuePairDto(List<UnitDto> units, List<ShipyardQueueEntryDto> queue) {
}
