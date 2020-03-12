package com.github.retro_game.retro_game.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "flight_view")
public class FlightView {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "start_user_id", nullable = false, insertable = false, updatable = false)
  private long startUserId;

  @Column(name = "start_user_name", nullable = false, insertable = false, updatable = false)
  private String startUserName;

  @Column(name = "start_body_id", nullable = false, insertable = false, updatable = false)
  private long startBodyId;

  @Column(name = "start_body_name", nullable = false, insertable = false, updatable = false)
  private String startBodyName;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "galaxy", column = @Column(name = "start_galaxy")),
      @AttributeOverride(name = "system", column = @Column(name = "start_system")),
      @AttributeOverride(name = "position", column = @Column(name = "start_position")),
      @AttributeOverride(name = "kind", column = @Column(name = "start_kind")),
  })
  private Coordinates startCoordinates;

  @Column(name = "target_user_id", insertable = false, updatable = false)
  private Long targetUserId;

  @Column(name = "target_user_name", insertable = false, updatable = false)
  private String targetUserName;

  @Column(name = "target_body_id", insertable = false, updatable = false)
  private Long targetBodyId;

  @Column(name = "target_body_name", insertable = false, updatable = false)
  private String targetBodyName;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "galaxy", column = @Column(name = "target_galaxy")),
      @AttributeOverride(name = "system", column = @Column(name = "target_system")),
      @AttributeOverride(name = "position", column = @Column(name = "target_position")),
      @AttributeOverride(name = "kind", column = @Column(name = "target_kind")),
  })
  private Coordinates targetCoordinates;

  @Column(name = "party_id", insertable = false, updatable = false)
  private Long partyId;

  @Column(name = "departure_at", nullable = false, insertable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date departureAt;

  @Column(name = "arrival_at", insertable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date arrivalAt;

  @Column(name = "return_at", nullable = false, insertable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date returnAt;

  @Column(name = "hold_until", insertable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date holdUntil;

  @Column(name = "mission", nullable = false, insertable = false, updatable = false)
  private Mission mission;

  @Embedded
  private Resources resources;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "key.flight")
  @MapKey(name = "key.kind")
  @Fetch(FetchMode.SUBSELECT)
  private Map<UnitKind, FlightUnit> units;

  public long getId() {
    return id;
  }

  public long getStartUserId() {
    return startUserId;
  }

  public String getStartUserName() {
    return startUserName;
  }

  public long getStartBodyId() {
    return startBodyId;
  }

  public String getStartBodyName() {
    return startBodyName;
  }

  public Coordinates getStartCoordinates() {
    return startCoordinates;
  }

  public Long getTargetUserId() {
    return targetUserId;
  }

  public String getTargetUserName() {
    return targetUserName;
  }

  public Long getTargetBodyId() {
    return targetBodyId;
  }

  public String getTargetBodyName() {
    return targetBodyName;
  }

  public Coordinates getTargetCoordinates() {
    return targetCoordinates;
  }

  public Long getPartyId() {
    return partyId;
  }

  public Date getDepartureAt() {
    return departureAt;
  }

  public Date getArrivalAt() {
    return arrivalAt;
  }

  public Date getReturnAt() {
    return returnAt;
  }

  public Date getHoldUntil() {
    return holdUntil;
  }

  public Mission getMission() {
    return mission;
  }

  public Resources getResources() {
    return resources;
  }

  public Map<UnitKind, FlightUnit> getUnits() {
    return units;
  }
}
