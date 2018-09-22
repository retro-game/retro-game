package com.github.retro_game.retro_game.service.dto;

public class RankingEntryDto {
  private final long userId;
  private final String userName;
  private final int points;
  private final int rank;

  public RankingEntryDto(long userId, String userName, int points, int rank) {
    this.userId = userId;
    this.userName = userName;
    this.points = points;
    this.rank = rank;
  }

  public long getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public int getPoints() {
    return points;
  }

  public int getRank() {
    return rank;
  }
}
