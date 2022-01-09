package com.github.retro_game.retro_game.dto;

public record BuildingDto(BuildingKindDto kind, int currentLevel, int futureLevel, ResourcesDto cost,
                          int requiredEnergy, long constructionTime, boolean canConstructNow) {
}
