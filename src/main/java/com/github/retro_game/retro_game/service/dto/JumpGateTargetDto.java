package com.github.retro_game.retro_game.service.dto;

import java.util.Date;

public class JumpGateTargetDto {
  private final long id;
  private final String name;
  private final CoordinatesDto coordinates;
  private final Date canJumpAt;

  public JumpGateTargetDto(long id, String name, CoordinatesDto coordinates, Date canJumpAt) {
    this.id = id;
    this.name = name;
    this.coordinates = coordinates;
    this.canJumpAt = canJumpAt;
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

  public Date getCanJumpAt() {
    return canJumpAt;
  }
}
