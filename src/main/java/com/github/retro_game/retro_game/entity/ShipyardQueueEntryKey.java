package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ShipyardQueueEntryKey implements Serializable {
  @JoinColumn(name = "body_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Body body;

  @Column(name = "sequence", nullable = false, updatable = false)
  private int sequence;

  public Body getBody() {
    return body;
  }

  public void setBody(Body body) {
    this.body = body;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ShipyardQueueEntryKey that = (ShipyardQueueEntryKey) o;
    return sequence == that.sequence && Objects.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, sequence);
  }
}
