package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Ranking {
  @Column(name = "user_id", nullable = false)
  @Id
  private long userId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "rank", nullable = false)
  private int rank;

  @Column(name = "points", nullable = false)
  private int points;

  public long getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public int getRank() {
    return rank;
  }

  public int getPoints() {
    return points;
  }
}
