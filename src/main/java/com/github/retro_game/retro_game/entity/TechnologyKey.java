package com.github.retro_game.retro_game.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TechnologyKey implements Serializable {
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Column(name = "kind", nullable = false, updatable = false)
  private TechnologyKind kind;

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public TechnologyKind getKind() {
    return kind;
  }

  public void setKind(TechnologyKind kind) {
    this.kind = kind;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TechnologyKey that = (TechnologyKey) o;
    return Objects.equals(user, that.user) && kind == that.kind;
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, kind);
  }
}

