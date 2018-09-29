package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.*;
import io.vavr.Tuple2;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.List;

public interface StatisticsService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  @Nullable
  StatisticsSummaryDto getCurrentUserSummary(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  @Nullable
  StatisticsSummaryDto getSummary(long bodyId, long userId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<Tuple2<Date, PointsAndRankPairDto>> getDistinctChanges(long bodyId, long userId, StatisticsKindDto kind,
                                                              StatisticsPeriodDto period);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<Tuple2<Date, StatisticsDistributionDto>> getDistributionChanges(long bodyId, long userId,
                                                                       StatisticsPeriodDto period);
}
