package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

public record GalaxySlotDto(long userId, String userName, int rank, boolean onVacation, boolean banned,
                            NoobProtectionRankDto noobProtectionRank,
                            String planetName, BodyTypeDto planetType, int planetImage,
                            @Nullable String moonName, @Nullable Integer moonImage,
                            int activity,
                            long debrisMetal, long debrisCrystal, int neededRecyclers,
                            @Nullable Long allianceId, @Nullable String allianceTag,
                            boolean own, boolean shortInactive, boolean longInactive) {
}
