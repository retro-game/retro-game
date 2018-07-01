package com.github.retro_game.retro_game.service.dto;

import java.io.Serializable;

public class CoordinatesDto implements Comparable<CoordinatesDto>, Serializable {
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
  public int compareTo(CoordinatesDto coordinates) {
    if (coordinates.galaxy != galaxy) return galaxy - coordinates.galaxy;
    if (coordinates.system != system) return system - coordinates.system;
    if (coordinates.position != position) return position - coordinates.position;
    if (coordinates.kind != kind) return kind.compareTo(coordinates.kind);
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CoordinatesDto that = (CoordinatesDto) o;
    return galaxy == that.galaxy &&
        system == that.system &&
        position == that.position &&
        kind == that.kind;
  }

  @Override
  public String toString() {
    return "" + galaxy + '-' + system + '-' + position + '-' + kind.getShortcut();
  }
}
