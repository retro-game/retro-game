package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "alliance_ranks")
public class AllianceRank {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "alliance_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Alliance alliance;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "privileges", nullable = false)
  private int privileges;

  public long getId() {
    return id;
  }

  public Alliance getAlliance() {
    return alliance;
  }

  public void setAlliance(Alliance alliance) {
    this.alliance = alliance;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPrivileges() {
    return privileges;
  }

  public void setPrivileges(int privileges) {
    this.privileges = privileges;
  }
}
