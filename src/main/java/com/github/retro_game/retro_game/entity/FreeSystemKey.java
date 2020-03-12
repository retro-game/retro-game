package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FreeSystemKey implements Serializable {
  @Column(name = "galaxy", nullable = false, insertable = false, updatable = false)
  private int galaxy;

  @Column(name = "system", nullable = false, insertable = false, updatable = false)
  private int system;

  public int getGalaxy() {
    return galaxy;
  }

  public int getSystem() {
    return system;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FreeSystemKey that = (FreeSystemKey) o;
    return galaxy == that.galaxy && system == that.system;
  }

  @Override
  public int hashCode() {
    return Objects.hash(galaxy, system);
  }
}
