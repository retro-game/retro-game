package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "building_queue")
public class BuildingQueueEntry {
  @EmbeddedId
  private BuildingQueueEntryKey key;

  @Column(name = "kind", nullable = false)
  private BuildingKind kind;

  @Column(name = "action", nullable = false)
  private BuildingQueueAction action;

  public BuildingQueueEntryKey getKey() {
    return key;
  }

  public void setKey(BuildingQueueEntryKey key) {
    this.key = key;
  }

  public Body getBody() {
    return key.getBody();
  }

  public int getSequence() {
    return key.getSequence();
  }

  public BuildingKind getKind() {
    return kind;
  }

  public void setKind(BuildingKind kind) {
    this.kind = kind;
  }

  public BuildingQueueAction getAction() {
    return action;
  }

  public void setAction(BuildingQueueAction action) {
    this.action = action;
  }
}
