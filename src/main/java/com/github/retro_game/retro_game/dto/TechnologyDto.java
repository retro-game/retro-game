package com.github.retro_game.retro_game.dto;

public record TechnologyDto(TechnologyKindDto kind, int currentLevel, int futureLevel, ResourcesDto cost,
                            int requiredEnergy, long researchTime, int effectiveLabLevel, boolean canResearchNow) {
}
