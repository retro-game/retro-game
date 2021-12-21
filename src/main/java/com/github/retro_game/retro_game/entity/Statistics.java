package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
public abstract class Statistics {
  @EmbeddedId
  private StatisticsKey key;

  @Column(name = "points", insertable = false, nullable = false, updatable = false)
  private long points;

  @Column(name = "rank", insertable = false, nullable = false, updatable = false)
  private int rank;

  public long getUserId() {
    return key.getUserId();
  }

  public Date getAt() {
    return key.getAt();
  }

  public StatisticsKey getKey() {
    return key;
  }

  public long getPoints() {
    return points;
  }

  public int getRank() {
    return rank;
  }
}
