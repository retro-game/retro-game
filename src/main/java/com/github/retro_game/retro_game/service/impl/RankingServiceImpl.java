package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.service.RankingService;
import com.github.retro_game.retro_game.service.dto.RankingDto;
import com.github.retro_game.retro_game.service.dto.StatisticsKindDto;
import com.github.retro_game.retro_game.service.impl.cache.StatisticsAndRankingCache;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
class RankingServiceImpl implements RankingService {
  private final StatisticsAndRankingCache statisticsAndRankingCache;

  public RankingServiceImpl(StatisticsAndRankingCache statisticsAndRankingCache) {
    this.statisticsAndRankingCache = statisticsAndRankingCache;
  }

  @Override
  public RankingDto getLatest(long bodyId, @Nullable StatisticsKindDto kind) {
    return statisticsAndRankingCache.getLatestRanking(kind);
  }
}
