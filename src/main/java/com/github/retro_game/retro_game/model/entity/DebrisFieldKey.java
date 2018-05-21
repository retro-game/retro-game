package com.github.retro_game.retro_game.model.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DebrisFieldKey implements Serializable {
  @Column(name = "galaxy", nullable = false, updatable = false)
  private int galaxy;

  @Column(name = "system", nullable = false, updatable = false)
  private int system;

  @Column(name = "position", nullable = false, updatable = false)
  private int position;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DebrisFieldKey that = (DebrisFieldKey) o;
    return galaxy == that.galaxy &&
        system == that.system &&
        position == that.position;
  }

  @Override
  public int hashCode() {
    return Objects.hash(galaxy, system, position);
  }
}
