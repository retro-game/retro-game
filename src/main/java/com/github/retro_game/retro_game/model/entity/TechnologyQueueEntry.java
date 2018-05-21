package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "technology_queue")
public class TechnologyQueueEntry {
  @EmbeddedId
  private TechnologyQueueEntryKey key;

  @JoinColumn(name = "body_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Body body;

  @Column(name = "kind", nullable = false)
  private TechnologyKind kind;

  public TechnologyQueueEntryKey getKey() {
    return key;
  }

  public void setKey(TechnologyQueueEntryKey key) {
    this.key = key;
  }

  public User getUser() {
    return key.getUser();
  }

  public int getSequence() {
    return key.getSequence();
  }

  public Body getBody() {
    return body;
  }

  public void setBody(Body body) {
    this.body = body;
  }

  public TechnologyKind getKind() {
    return kind;
  }

  public void setKind(TechnologyKind kind) {
    this.kind = kind;
  }
}
