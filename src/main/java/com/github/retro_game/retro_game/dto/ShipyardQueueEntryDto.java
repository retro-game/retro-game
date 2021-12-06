package com.github.retro_game.retro_game.dto;

import java.util.Date;

public record ShipyardQueueEntryDto(UnitKindDto kind, int count, ResourcesDto cost, Date finishAt) {
}
