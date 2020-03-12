package com.github.retro_game.retro_game.dto;

public class PartyTargetDto {
  private final long id;
  private final String targetBodyName;
  private final CoordinatesDto targetCoordinates;

  public PartyTargetDto(long id, String targetBodyName, CoordinatesDto targetCoordinates) {
    this.id = id;
    this.targetBodyName = targetBodyName;
    this.targetCoordinates = targetCoordinates;
  }

  public long getId() {
    return id;
  }

  public String getTargetBodyName() {
    return targetBodyName;
  }

  public CoordinatesDto getTargetCoordinates() {
    return targetCoordinates;
  }
}
