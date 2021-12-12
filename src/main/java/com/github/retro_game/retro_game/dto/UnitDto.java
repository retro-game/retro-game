package com.github.retro_game.retro_game.dto;

public record UnitDto(UnitKindDto kind, int currentCount, int futureCount, ResourcesDto cost, long buildingTime,
                      int maxBuildable) {
}
