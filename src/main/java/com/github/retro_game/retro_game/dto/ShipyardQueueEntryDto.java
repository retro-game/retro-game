package com.github.retro_game.retro_game.dto;

import java.util.Date;

public record ShipyardQueueEntryDto(UnitKindDto kind, int count, int sequence, ResourcesDto cost, Date finishAt) {}
