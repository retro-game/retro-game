package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transport_reports")
public class TransportReport {
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

  @JoinColumn(name = "partner_id", updatable = false)
  private Long partnerId;

  @Column(name = "partner_name", nullable = false, updatable = false)
  private String partnerName;

  @Column(name = "kind", nullable = false, updatable = false)
  private TransportKind kind;

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
      @AttributeOverride(name = "metal", column = @Column(name = "metal", nullable = false, updatable = false)),
      @AttributeOverride(name = "crystal", column = @Column(name = "crystal", nullable = false, updatable = false)),
      @AttributeOverride(name = "deuterium", column = @Column(name = "deuterium", nullable = false, updatable = false)),
  })
  private Resources resources;

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

  public Long getPartnerId() {
    return partnerId;
  }

  public void setPartnerId(Long partnerId) {
    this.partnerId = partnerId;
  }

  public String getPartnerName() {
    return partnerName;
  }

  public void setPartnerName(String partnerName) {
    this.partnerName = partnerName;
  }

  public TransportKind getKind() {
    return kind;
  }

  public void setKind(TransportKind kind) {
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
}
