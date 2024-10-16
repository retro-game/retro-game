package com.github.retro_game.retro_game.dto;

import java.util.Date;
import java.util.List;

public record RankingDto(Date updatedAt, List<RankingEntryDto> entries) {
}
