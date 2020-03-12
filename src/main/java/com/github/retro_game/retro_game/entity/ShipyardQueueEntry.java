package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "shipyard_queue")
public class ShipyardQueueEntry {
  @EmbeddedId
  private ShipyardQueueEntryKey key;

  @Column(name = "kind", nullable = false, updatable = false)
  private UnitKind kind;

  @Column(name = "count", nullable = false)
  private int count;

  public ShipyardQueueEntryKey getKey() {
    return key;
  }

  public void setKey(ShipyardQueueEntryKey key) {
    this.key = key;
  }

  public Body getBody() {
    return key.getBody();
  }

  public int getSequence() {
    return key.getSequence();
  }

  public UnitKind getKind() {
    return kind;
  }

  public void setKind(UnitKind kind) {
    this.kind = kind;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
