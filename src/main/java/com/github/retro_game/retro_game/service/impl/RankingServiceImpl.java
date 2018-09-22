package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.service.RankingService;
import com.github.retro_game.retro_game.service.dto.RankingDto;
import com.github.retro_game.retro_game.service.dto.RankingKindDto;
import com.github.retro_game.retro_game.service.impl.cache.RankingCache;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
class RankingServiceImpl implements RankingService {
  private final RankingCache rankingCache;

  public RankingServiceImpl(RankingCache rankingCache) {
    this.rankingCache = rankingCache;
  }

  @Override
  public RankingDto getLatest(long bodyId, @Nullable RankingKindDto kind) {
    return rankingCache.getLatest(kind);
  }
}
