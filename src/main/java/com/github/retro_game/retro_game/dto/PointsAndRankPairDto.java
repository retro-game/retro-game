package com.github.retro_game.retro_game.dto;

public class PointsAndRankPairDto {
  private final int points;
  private final int rank;

  public PointsAndRankPairDto(int points, int rank) {
    this.points = points;
    this.rank = rank;
  }

  public int getPoints() {
    return points;
  }

  public int getRank() {
    return rank;
  }
}
