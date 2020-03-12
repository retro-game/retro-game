package com.github.retro_game.retro_game.dto;

public class TechnologyDetailsDto {
  private final int currentLevel;
  private final int futureLevel;

  public TechnologyDetailsDto(int currentLevel, int futureLevel) {
    this.currentLevel = currentLevel;
    this.futureLevel = futureLevel;
  }

  public int getCurrentLevel() {
    return currentLevel;
  }

  public int getFutureLevel() {
    return futureLevel;
  }
}
