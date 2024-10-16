package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

public class OverviewBodyBasicInfoDto {
  private final long id;
  private final CoordinatesDto coordinates;
  private final String name;
  private final BodyTypeDto type;
  private final int image;
  private final BuildingKindDto ongoingBuildingKind;
  private final int ongoingBuildingLevel;

  public OverviewBodyBasicInfoDto(long id, CoordinatesDto coordinates, String name, BodyTypeDto type, int image,
                                  @Nullable BuildingKindDto ongoingBuildingKind, int ongoingBuildingLevel) {
    this.id = id;
    this.coordinates = coordinates;
    this.name = name;
    this.type = type;
    this.image = image;
    this.ongoingBuildingKind = ongoingBuildingKind;
    this.ongoingBuildingLevel = ongoingBuildingLevel;
  }

  public long getId() {
    return id;
  }

  public CoordinatesDto getCoordinates() {
    return coordinates;
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
