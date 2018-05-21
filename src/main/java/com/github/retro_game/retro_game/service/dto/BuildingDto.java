package com.github.retro_game.retro_game.service.dto;

public class BuildingDto {
  private final BuildingKindDto kind;
  private final int currentLevel;
  private final int futureLevel;
  private final ResourcesDto cost;
  private final int requiredEnergy;
  private final long constructionTime;
  private final boolean canConstructNow;

  public BuildingDto(BuildingKindDto kind, int currentLevel, int futureLevel, ResourcesDto cost, int requiredEnergy,
                     long constructionTime, boolean canConstructNow) {
    this.kind = kind;
    this.currentLevel = currentLevel;
    this.futureLevel = futureLevel;
    this.cost = cost;
    this.requiredEnergy = requiredEnergy;
    this.constructionTime = constructionTime;
    this.canConstructNow = canConstructNow;
  }

  public BuildingKindDto getKind() {
    return kind;
  }

  public int getCurrentLevel() {
    return currentLevel;
  }

  public int getFutureLevel() {
    return futureLevel;
  }

  public ResourcesDto getCost() {
    return cost;
  }

  public int getRequiredEnergy() {
    return requiredEnergy;
  }

  public long getConstructionTime() {
    return constructionTime;
  }

  public boolean isCanConstructNow() {
    return canConstructNow;
  }
}
