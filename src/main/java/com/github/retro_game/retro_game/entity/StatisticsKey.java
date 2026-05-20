package com.github.retro_game.retro_game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class StatisticsKey implements Serializable {
  @Column(name = "user_id", insertable = false, nullable = false, updatable = false)
  private long userId;

  @Column(name = "at", insertable = false, nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  public long getUserId() {
    return userId;
  }

  public Date getAt() {
    return at;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StatisticsKey that = (StatisticsKey) o;
    return userId == that.userId && Objects.equals(at, that.at);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, at);
  }
}
