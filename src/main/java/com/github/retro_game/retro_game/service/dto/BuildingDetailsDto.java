package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

public class BuildingDetailsDto {
  private final int currentLevel;
  private final int futureLevel;
  private final ResourcesDto destructionCost;
  private final long destructionTime;
  private final boolean destroyable;
  private final boolean canDestroyNow;

  public BuildingDetailsDto(int currentLevel, int futureLevel, @Nullable ResourcesDto destructionCost,
                            long destructionTime, boolean destroyable, boolean canDestroyNow) {
    this.currentLevel = currentLevel;
    this.futureLevel = futureLevel;
    this.destructionCost = destructionCost;
    this.destructionTime = destructionTime;
    this.destroyable = destroyable;
    this.canDestroyNow = canDestroyNow;
  }

  public int getCurrentLevel() {
    return currentLevel;
  }

  public int getFutureLevel() {
    return futureLevel;
  }

  public ResourcesDto getDestructionCost() {
    return destructionCost;
  }

  public long getDestructionTime() {
    return destructionTime;
  }

  public boolean isDestroyable() {
    return destroyable;
  }

  public boolean isCanDestroyNow() {
    return canDestroyNow;
  }
}
