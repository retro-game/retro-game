package com.github.retro_game.retro_game.dto;

import java.util.List;

public record TechnologiesAndQueuePairDto(List<TechnologyDto> technologies, List<TechnologyQueueEntryDto> queue) {
}
