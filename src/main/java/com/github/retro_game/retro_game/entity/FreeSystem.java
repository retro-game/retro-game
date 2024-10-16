package com.github.retro_game.retro_game.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "free_systems")
public class FreeSystem {
  @EmbeddedId
  private FreeSystemKey key;

  public FreeSystemKey getKey() {
    return key;
  }

  public int getGalaxy() {
    return key.getGalaxy();
  }

  public int getSystem() {
    return key.getSystem();
  }
}
