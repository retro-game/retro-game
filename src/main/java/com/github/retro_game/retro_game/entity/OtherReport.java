package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "other_reports")
public class OtherReport {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @Column(name = "kind", nullable = false, updatable = false)
  private OtherReportKind kind;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "galaxy",
          column = @Column(name = "start_galaxy", nullable = false, updatable = false)),
      @AttributeOverride(name = "system",
          column = @Column(name = "start_system", nullable = false, updatable = false)),
      @AttributeOverride(name = "position",
          column = @Column(name = "start_position", nullable = false, updatable = false)),
      @AttributeOverride(name = "kind",
          column = @Column(name = "start_kind", nullable = false, updatable = false)),
  })
  private Coordinates startCoordinates;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "galaxy",
          column = @Column(name = "target_galaxy", nullable = false, updatable = false)),
      @AttributeOverride(name = "system",
          column = @Column(name = "target_system", nullable = false, updatable = false)),
      @AttributeOverride(name = "position",
          column = @Column(name = "target_position", nullable = false, updatable = false)),
      @AttributeOverride(name = "kind",
          column = @Column(name = "target_kind", nullable = false, updatable = false)),
  })
  private Coordinates targetCoordinates;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "metal", column = @Column(name = "metal", updatable = false)),
      @AttributeOverride(name = "crystal", column = @Column(name = "crystal", updatable = false)),
      @AttributeOverride(name = "deuterium", column = @Column(name = "deuterium", updatable = false)),
  })
  private Resources resources;

  @Column(name = "param", updatable = false)
  private Double param;

  public long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Date getAt() {
    return at;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public OtherReportKind getKind() {
    return kind;
  }

  public void setKind(OtherReportKind kind) {
    this.kind = kind;
  }

  public Coordinates getStartCoordinates() {
    return startCoordinates;
  }

  public void setStartCoordinates(Coordinates startCoordinates) {
    this.startCoordinates = startCoordinates;
  }

  public Coordinates getTargetCoordinates() {
    return targetCoordinates;
  }

  public void setTargetCoordinates(Coordinates targetCoordinates) {
    this.targetCoordinates = targetCoordinates;
  }

  public Resources getResources() {
    return resources;
  }

  public void setResources(Resources resources) {
    this.resources = resources;
  }

  public Double getParam() {
    return param;
  }

  public void setParam(Double param) {
    this.param = param;
  }
}
