package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import io.vavr.Tuple2;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

public interface StatisticsService {
  @Activity(bodies = "#bodyId")
  RankingDto getLatestRanking(long bodyId, StatisticsKindDto kind);

  @Activity(bodies = "#bodyId")
  @Nullable
  StatisticsSummaryDto getCurrentUserSummary(long bodyId);

  @Activity(bodies = "#bodyId")
  @Nullable
  StatisticsSummaryDto getSummary(long bodyId, long userId);

  @Activity(bodies = "#bodyId")
  List<Tuple2<Date, PointsAndRankPairDto>> getDistinctChanges(long bodyId, long userId, StatisticsKindDto kind,
                                                              StatisticsPeriodDto period);

  @Activity(bodies = "#bodyId")
  List<Tuple2<Date, StatisticsDistributionDto>> getDistributionChanges(long bodyId, long userId,
                                                                       StatisticsPeriodDto period);
}
