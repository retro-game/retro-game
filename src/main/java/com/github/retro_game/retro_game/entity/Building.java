package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "buildings")
public class Building {
  @EmbeddedId
  private BuildingKey key;

  @Column(name = "level", nullable = false)
  private int level;

  public BuildingKey getKey() {
    return key;
  }

  public void setKey(BuildingKey key) {
    this.key = key;
  }

  public Body getBody() {
    return key.getBody();
  }

  public BuildingKind getKind() {
    return key.getKind();
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }
}
