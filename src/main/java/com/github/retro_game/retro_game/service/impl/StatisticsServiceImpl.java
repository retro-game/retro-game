package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.StatisticsService;
import com.github.retro_game.retro_game.service.dto.UserStatisticsDto;
import com.github.retro_game.retro_game.service.impl.cache.StatisticsAndRankingCache;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {
  private final StatisticsAndRankingCache statisticsAndRankingCache;

  public StatisticsServiceImpl(StatisticsAndRankingCache statisticsAndRankingCache) {
    this.statisticsAndRankingCache = statisticsAndRankingCache;
  }

  @Override
  public UserStatisticsDto getUserStatistics(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    return statisticsAndRankingCache.getUserStatistics(userId);
  }
}
