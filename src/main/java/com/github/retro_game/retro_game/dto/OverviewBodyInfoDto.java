package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class OverviewBodyInfoDto {
  private final long id;
  private final CoordinatesDto coordinates;
  private final String name;
  private final int diameter;
  private final int temperature;
  private final BodyTypeDto type;
  private final int image;
  private final BuildingKindDto ongoingBuildingKind;
  private final int ongoingBuildingLevel;
  private final Date ongoingBuildingFinishAt;
  private final int usedFields;
  private final int maxFields;

  public OverviewBodyInfoDto(long id, CoordinatesDto coordinates, String name, int diameter, int temperature,
                             BodyTypeDto type, int image, @Nullable BuildingKindDto ongoingBuildingKind,
                             int ongoingBuildingLevel, @Nullable Date ongoingBuildingFinishAt, int usedFields,
                             int maxFields) {
    this.id = id;
    this.coordinates = coordinates;
    this.name = name;
    this.diameter = diameter;
    this.temperature = temperature;
    this.type = type;
    this.image = image;
    this.ongoingBuildingKind = ongoingBuildingKind;
    this.ongoingBuildingLevel = ongoingBuildingLevel;
    this.ongoingBuildingFinishAt = ongoingBuildingFinishAt;
    this.usedFields = usedFields;
    this.maxFields = maxFields;
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

  public int getDiameter() {
    return diameter;
  }

  public int getTemperature() {
    return temperature;
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

  public Date getOngoingBuildingFinishAt() {
    return ongoingBuildingFinishAt;
  }

  public int getUsedFields() {
    return usedFields;
  }

  public int getMaxFields() {
    return maxFields;
  }
}
