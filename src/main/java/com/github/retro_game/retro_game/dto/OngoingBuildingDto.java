package com.github.retro_game.retro_game.dto;

import com.github.retro_game.retro_game.entity.BuildingKind;

public class OngoingBuildingDto {
  private final BuildingKind kind;
  private final int level;

  public OngoingBuildingDto(BuildingKind kind, int level) {
    this.kind = kind;
    this.level = level;
  }

  public BuildingKind getKind() {
    return kind;
  }

  public int getLevel() {
    return level;
  }
}
