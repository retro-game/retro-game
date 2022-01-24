package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public record TechnologyDto(TechnologyKindDto kind, int currentLevel, int futureLevel, ResourcesDto cost,
                            int requiredEnergy, long researchTime, int effectiveLabLevel, ResourcesDto missingResources,
                            long neededSmallCargoes, long neededLargeCargoes, @Nullable Date accumulationTime,
                            boolean canResearchNow) {
}
