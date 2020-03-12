package com.github.retro_game.retro_game.dto;

public class UnitDto {
  private final UnitKindDto kind;
  private final int currentCount;
  private final int futureCount;
  private final ResourcesDto cost;
  private final long buildingTime;
  private final int maxBuildable;

  public UnitDto(UnitKindDto kind, int currentCount, int futureCount, ResourcesDto cost, long buildingTime,
                 int maxBuildable) {
    this.kind = kind;
    this.currentCount = currentCount;
    this.futureCount = futureCount;
    this.cost = cost;
    this.buildingTime = buildingTime;
    this.maxBuildable = maxBuildable;
  }

  public UnitKindDto getKind() {
    return kind;
  }

  public int getCurrentCount() {
    return currentCount;
  }

  public int getFutureCount() {
    return futureCount;
  }

  public ResourcesDto getCost() {
    return cost;
  }

  public long getBuildingTime() {
    return buildingTime;
  }

  public int getMaxBuildable() {
    return maxBuildable;
  }
}
