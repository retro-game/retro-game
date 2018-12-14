package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "alliance_members")
public class AllianceMember {
  @EmbeddedId
  private AllianceMemberKey key;

  @JoinColumn(name = "rank_id", updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private AllianceRank rank;

  @Column(name = "joined_at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date joinedAt;

  public Alliance getAlliance() {
    return key.getAlliance();
  }

  public User getUser() {
    return key.getUser();
  }

  public AllianceMemberKey getKey() {
    return key;
  }

  public void setKey(AllianceMemberKey key) {
    this.key = key;
  }

  public AllianceRank getRank() {
    return rank;
  }

  public void setRank(AllianceRank rank) {
    this.rank = rank;
  }

  public Date getJoinedAt() {
    return joinedAt;
  }

  public void setJoinedAt(Date joinedAt) {
    this.joinedAt = joinedAt;
  }
}
