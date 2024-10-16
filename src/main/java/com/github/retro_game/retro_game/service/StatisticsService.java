package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import io.vavr.Tuple2;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

public interface StatisticsService {
  RankingDto getLatestRanking(long bodyId, StatisticsKindDto kind);

  @Nullable
  StatisticsSummaryDto getCurrentUserSummary(long bodyId);

  @Nullable
  StatisticsSummaryDto getSummary(long bodyId, long userId);

  List<Tuple2<Date, PointsAndRankPairDto>> getDistinctChanges(long bodyId, long userId, StatisticsKindDto kind,
                                                              StatisticsPeriodDto period);

  List<Tuple2<Date, StatisticsDistributionDto>> getDistributionChanges(long bodyId, long userId,
                                                                       StatisticsPeriodDto period);
}
