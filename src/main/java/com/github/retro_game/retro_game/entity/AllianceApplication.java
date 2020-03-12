package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "alliance_applications")
public class AllianceApplication {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "alliance_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Alliance alliance;

  @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
  @OneToOne(fetch = FetchType.LAZY)
  private User user;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @Column(name = "application_text", nullable = false, updatable = false)
  private String applicationText;

  public long getId() {
    return id;
  }

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

  public Date getAt() {
    return at;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public String getApplicationText() {
    return applicationText;
  }

  public void setApplicationText(String applicationText) {
    this.applicationText = applicationText;
  }
}
