package com.github.retro_game.retro_game.dto;

public class BodyInfoDto {
  private final long id;
  private final long userId;
  private final String name;
  private final CoordinatesDto coordinates;

  public BodyInfoDto(long id, long userId, String name, CoordinatesDto coordinates) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.coordinates = coordinates;
  }

  public long getId() {
    return id;
  }

  public long getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public CoordinatesDto getCoordinates() {
    return coordinates;
  }
}
