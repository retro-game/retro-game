package com.github.retro_game.retro_game.service.dto;

import java.io.Serializable;

public class CoordinatesDto implements Serializable {
  private final int galaxy;
  private final int system;
  private final int position;
  private final CoordinatesKindDto kind;

  public CoordinatesDto(int galaxy, int system, int position, CoordinatesKindDto kind) {
    this.galaxy = galaxy;
    this.system = system;
    this.position = position;
    this.kind = kind;
  }

  public int getGalaxy() {
    return galaxy;
  }

  public int getSystem() {
    return system;
  }

  public int getPosition() {
    return position;
  }

  public CoordinatesKindDto getKind() {
    return kind;
  }

  @Override
  public String toString() {
    return "" + galaxy + '-' + system + '-' + position + '-' + kind.getShortcut();
  }
}
