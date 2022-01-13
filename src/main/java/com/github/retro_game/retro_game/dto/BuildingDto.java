package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public record BuildingDto(BuildingKindDto kind, int currentLevel, int futureLevel, ResourcesDto cost,
                          int requiredEnergy, long constructionTime, ResourcesDto missingResources,
                          long neededSmallCargoes, long neededLargeCargoes, @Nullable Date costAccumulatedAt,
                          boolean canConstructNow) {
}
