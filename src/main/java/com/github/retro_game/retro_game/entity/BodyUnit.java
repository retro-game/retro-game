package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "body_units")
public class BodyUnit {
  @EmbeddedId
  private BodyUnitKey key;

  @Column(name = "count", nullable = false)
  private int count;

  public BodyUnitKey getKey() {
    return key;
  }

  public void setKey(BodyUnitKey key) {
    this.key = key;
  }

  public Body getBody() {
    return key.getBody();
  }

  public UnitKind getKind() {
    return key.getKind();
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
