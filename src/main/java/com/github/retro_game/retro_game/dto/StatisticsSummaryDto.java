package com.github.retro_game.retro_game.dto;

public record StatisticsSummaryDto(PointsAndRankPairDto overall, PointsAndRankPairDto buildings,
                                   PointsAndRankPairDto technologies, PointsAndRankPairDto fleet,
                                   PointsAndRankPairDto defense) {
}
