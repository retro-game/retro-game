package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.cache.StatisticsCache;
import com.github.retro_game.retro_game.dto.NoobProtectionRankDto;
import com.github.retro_game.retro_game.dto.StatisticsSummaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class NoobProtectionServiceImpl implements NoobProtectionService {
  private final boolean noobProtectionEnabled;
  private final int noobProtectionMaxPoints;
  private final int noobProtectionMultiplier;
  private final StatisticsCache statisticsCache;
  private ActivityService activityService;

  public NoobProtectionServiceImpl(@Value("${retro-game.noob-protection-enabled}") boolean noobProtectionEnabled,
                                   @Value("${retro-game.noob-protection-max-points}") int noobProtectionMaxPoints,
                                   @Value("${retro-game.noob-protection-multiplier}") int noobProtectionMultiplier,
                                   StatisticsCache statisticsCache) {
    this.noobProtectionEnabled = noobProtectionEnabled;
    this.noobProtectionMaxPoints = noobProtectionMaxPoints;
    this.noobProtectionMultiplier = noobProtectionMultiplier;
    this.statisticsCache = statisticsCache;
  }

  @Autowired
  public void setActivityService(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public NoobProtectionRankDto getOtherPlayerRank(long selfId, long otherId) {
    if (!noobProtectionEnabled)
      return NoobProtectionRankDto.EQUAL;

    if (activityService.isInactive(otherId))
      return NoobProtectionRankDto.EQUAL;

    // If the other player is new (has no statistics), then the player is always noob.
    StatisticsSummaryDto otherSummary = statisticsCache.getUserSummary(otherId);
    if (otherSummary == null)
      return NoobProtectionRankDto.NOOB;

    // If the self player is new (has no statistics), then all other players are strong.
    StatisticsSummaryDto selfSummary = statisticsCache.getUserSummary(selfId);
    if (selfSummary == null)
      return NoobProtectionRankDto.STRONG;

    long otherPoints = otherSummary.getOverall().getPoints();
    long selfPoints = selfSummary.getOverall().getPoints();

    // Check whether the both players are not protected anymore.
    if (Math.min(otherPoints, selfPoints) > noobProtectionMaxPoints)
      return NoobProtectionRankDto.EQUAL;

    if (selfPoints > noobProtectionMultiplier * otherPoints)
      return NoobProtectionRankDto.NOOB;
    if (selfPoints * noobProtectionMultiplier < otherPoints)
      return NoobProtectionRankDto.STRONG;
    return NoobProtectionRankDto.EQUAL;
  }
}
