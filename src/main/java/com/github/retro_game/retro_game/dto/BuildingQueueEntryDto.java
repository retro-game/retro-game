package com.github.retro_game.retro_game.dto;

import java.util.Date;

public record BuildingQueueEntryDto(BuildingKindDto kind, int sequence, int levelFrom, int levelTo, ResourcesDto cost,
                                    int requiredEnergy, Date finishAt, boolean downMovable, boolean upMovable,
                                    boolean cancelable) {}
