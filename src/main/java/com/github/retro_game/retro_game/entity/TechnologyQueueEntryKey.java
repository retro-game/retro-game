package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TechnologyQueueEntryKey implements Serializable {
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Column(name = "sequence", nullable = false, updatable = false)
  private int sequence;

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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
    TechnologyQueueEntryKey that = (TechnologyQueueEntryKey) o;
    return sequence == that.sequence && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, sequence);
  }
}
