package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.RankingDto;
import com.github.retro_game.retro_game.service.dto.StatisticsKindDto;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;

public interface RankingService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  RankingDto getLatest(long bodyId, @Nullable StatisticsKindDto kind);
}
