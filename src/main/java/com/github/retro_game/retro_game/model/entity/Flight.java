package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "flights")
public class Flight {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "start_user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User startUser;

  @JoinColumn(name = "start_body_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Body startBody;

  @JoinColumn(name = "target_user_id", updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User targetUser;

  @JoinColumn(name = "target_body_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Body targetBody;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "galaxy",
          column = @Column(name = "target_galaxy", nullable = false, updatable = false)),
      @AttributeOverride(name = "system",
          column = @Column(name = "target_system", nullable = false, updatable = false)),
      @AttributeOverride(name = "position",
          column = @Column(name = "target_position", nullable = false, updatable = false)),
      // After destruction of a moon, all flights targeting that moon change their kind to planet.
      @AttributeOverride(name = "kind",
          column = @Column(name = "target_kind", nullable = false)),
  })
  private Coordinates targetCoordinates;

  @JoinColumn(name = "party_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Party party;

  @Column(name = "departure_at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date departureAt;

  @Column(name = "arrival_at")
  @Temporal(TemporalType.TIMESTAMP)
  private Date arrivalAt;

  @Column(name = "return_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date returnAt;

  @Column(name = "hold_until")
  @Temporal(TemporalType.TIMESTAMP)
  private Date holdUntil;

  @Column(name = "mission", nullable = false, updatable = false)
  private Mission mission;

  @Embedded
  private Resources resources;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "key.flight")
  @MapKey(name = "key.kind")
  private Map<UnitKind, FlightUnit> units;

  public long getId() {
    return id;
  }

  public User getStartUser() {
    return startUser;
  }

  public void setStartUser(User startUser) {
    this.startUser = startUser;
  }

  public Body getStartBody() {
    return startBody;
  }

  public void setStartBody(Body startBody) {
    this.startBody = startBody;
  }

  public User getTargetUser() {
    return targetUser;
  }

  public void setTargetUser(User targetUser) {
    this.targetUser = targetUser;
  }

  public Body getTargetBody() {
    return targetBody;
  }

  public void setTargetBody(Body targetBody) {
    this.targetBody = targetBody;
  }

  public Coordinates getTargetCoordinates() {
    return targetCoordinates;
  }

  public void setTargetCoordinates(Coordinates targetCoordinates) {
    this.targetCoordinates = targetCoordinates;
  }

  public Party getParty() {
    return party;
  }

  public void setParty(Party party) {
    this.party = party;
  }

  public Date getDepartureAt() {
    return departureAt;
  }

  public void setDepartureAt(Date departureAt) {
    this.departureAt = departureAt;
  }

  public Date getArrivalAt() {
    return arrivalAt;
  }

  public void setArrivalAt(Date arrivalAt) {
    this.arrivalAt = arrivalAt;
  }

  public Date getReturnAt() {
    return returnAt;
  }

  public void setReturnAt(Date returnAt) {
    this.returnAt = returnAt;
  }

  public Date getHoldUntil() {
    return holdUntil;
  }

  public void setHoldUntil(Date holdUntil) {
    this.holdUntil = holdUntil;
  }

  public Mission getMission() {
    return mission;
  }

  public void setMission(Mission mission) {
    this.mission = mission;
  }

  public Resources getResources() {
    return resources;
  }

  public void setResources(Resources resources) {
    this.resources = resources;
  }

  public Map<UnitKind, FlightUnit> getUnits() {
    return units;
  }
}
