package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.UserStatisticsDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface StatisticsService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  UserStatisticsDto getUserStatistics(long bodyId);
}
