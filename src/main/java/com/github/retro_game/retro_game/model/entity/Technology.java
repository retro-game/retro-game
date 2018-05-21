package com.github.retro_game.retro_game.model.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "technologies")
public class Technology {
  @EmbeddedId
  private TechnologyKey key;

  @Column(name = "level", nullable = false)
  private int level;

  public TechnologyKey getKey() {
    return key;
  }

  public void setKey(TechnologyKey key) {
    this.key = key;
  }

  public User getUser() {
    return key.getUser();
  }

  public TechnologyKind getKind() {
    return key.getKind();
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }
}
