package com.github.retro_game.retro_game.dto;

import java.util.List;

public record BuildingsAndQueuePairDto(List<BuildingDto> buildings, List<BuildingQueueEntryDto> queue) {
}
