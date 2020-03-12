package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AllianceMemberKey implements Serializable {
  @JoinColumn(name = "alliance_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Alliance alliance;

  @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
  @OneToOne(fetch = FetchType.LAZY)
  private User user;

  public Alliance getAlliance() {
    return alliance;
  }

  public void setAlliance(Alliance alliance) {
    this.alliance = alliance;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AllianceMemberKey that = (AllianceMemberKey) o;
    return alliance.equals(that.alliance) && user.equals(that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alliance, user);
  }
}
