package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.BuildingKind;

class OngoingBuildingDto {
  private final BuildingKind kind;
  private final int level;

  OngoingBuildingDto(BuildingKind kind, int level) {
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
