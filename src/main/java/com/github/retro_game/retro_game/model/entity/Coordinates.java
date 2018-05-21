package com.github.retro_game.retro_game.model.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Coordinates implements Comparable<Coordinates> {
  @Column(name = "galaxy", nullable = false, updatable = false)
  private int galaxy;

  @Column(name = "system", nullable = false, updatable = false)
  private int system;

  @Column(name = "position", nullable = false, updatable = false)
  private int position;

  @Column(name = "kind", nullable = false, updatable = false)
  private CoordinatesKind kind;

  public Coordinates() {
  }

  public Coordinates(Coordinates c) {
    this.galaxy = c.galaxy;
    this.system = c.system;
    this.position = c.position;
    this.kind = c.kind;
  }

  public Coordinates(int galaxy, int system, int position, CoordinatesKind kind) {
    this.galaxy = galaxy;
    this.system = system;
    this.position = position;
    this.kind = kind;
  }

  public int getGalaxy() {
    return galaxy;
  }

  public void setGalaxy(int galaxy) {
    this.galaxy = galaxy;
  }

  public int getSystem() {
    return system;
  }

  public void setSystem(int system) {
    this.system = system;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public CoordinatesKind getKind() {
    return kind;
  }

  public void setKind(CoordinatesKind kind) {
    this.kind = kind;
  }

  @Override
  public int compareTo(Coordinates coordinates) {
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
    Coordinates that = (Coordinates) o;
    return galaxy == that.galaxy &&
        system == that.system &&
        position == that.position &&
        kind == that.kind;
  }

  @Override
  public int hashCode() {
    return Objects.hash(galaxy, system, position, kind);
  }

  @Override
  public String toString() {
    return "" + galaxy + '-' + system + '-' + position + '-' + kind.getShortcut();
  }
}
