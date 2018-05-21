package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

public class OverviewBodyBasicInfoDto {
  private final long id;
  private final String name;
  private final BodyTypeDto type;
  private final int image;
  private final BuildingKindDto ongoingBuildingKind;
  private final int ongoingBuildingLevel;

  public OverviewBodyBasicInfoDto(long id, String name, BodyTypeDto type, int image,
                                  @Nullable BuildingKindDto ongoingBuildingKind, int ongoingBuildingLevel) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.image = image;
    this.ongoingBuildingKind = ongoingBuildingKind;
    this.ongoingBuildingLevel = ongoingBuildingLevel;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BodyTypeDto getType() {
    return type;
  }

  public int getImage() {
    return image;
  }

  public BuildingKindDto getOngoingBuildingKind() {
    return ongoingBuildingKind;
  }

  public int getOngoingBuildingLevel() {
    return ongoingBuildingLevel;
  }
}
