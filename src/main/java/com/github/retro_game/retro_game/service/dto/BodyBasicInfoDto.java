package com.github.retro_game.retro_game.service.dto;

import java.io.Serializable;

// Serializable is necessary, as objects of this class may be cached.
public class BodyBasicInfoDto implements Serializable {
  private final long id;
  private final String name;
  private final CoordinatesDto coordinates;

  public BodyBasicInfoDto(long id, String name, CoordinatesDto coordinates) {
    this.id = id;
    this.name = name;
    this.coordinates = coordinates;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public CoordinatesDto getCoordinates() {
    return coordinates;
  }
}
