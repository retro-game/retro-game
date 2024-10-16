package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public record TransportReportDto(long id,
                                 long userId,
                                 Date at,
                                 TransportKindDto kind,
                                 @Nullable Long partnerId,
                                 String partnerName,
                                 CoordinatesDto startCoordinates,
                                 CoordinatesDto targetCoordinates,
                                 ResourcesDto resources) {
}
