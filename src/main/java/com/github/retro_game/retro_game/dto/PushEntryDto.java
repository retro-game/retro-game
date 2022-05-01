package com.github.retro_game.retro_game.dto;

public record PushEntryDto(TransportReportAndPointsDto reportAndPoints, long userReceived, long partnerReceived) {
}
