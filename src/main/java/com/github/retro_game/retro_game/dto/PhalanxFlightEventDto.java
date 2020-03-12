package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.Map;

public class PhalanxFlightEventDto {
  private final long id;
  private final Date at;
  private final long startUserId;
  private final String startUserName;
  private final String startBodyName;
  private final CoordinatesDto startCoordinates;
  private final String targetBodyName;
  private final CoordinatesDto targetCoordinates;
  private final Long partyId;
  private final MissionDto mission;
  private final Map<UnitKindDto, Integer> units;
  private final boolean own;
  private final FlightEventKindDto kind;

  public PhalanxFlightEventDto(long id, Date at, long startUserId, String startUserName, String startBodyName,
                               CoordinatesDto startCoordinates, @Nullable String targetBodyName,
                               CoordinatesDto targetCoordinates, @Nullable Long partyId, MissionDto mission,
                               Map<UnitKindDto, Integer> units, boolean own, FlightEventKindDto kind) {
    this.id = id;
    this.at = at;
    this.startUserId = startUserId;
    this.startUserName = startUserName;
    this.startBodyName = startBodyName;
    this.startCoordinates = startCoordinates;
    this.targetBodyName = targetBodyName;
    this.targetCoordinates = targetCoordinates;
    this.partyId = partyId;
    this.mission = mission;
    this.units = units;
    this.own = own;
    this.kind = kind;
  }

  public long getId() {
    return id;
  }

  public Date getAt() {
    return at;
  }

  public long getStartUserId() {
    return startUserId;
  }

  public String getStartUserName() {
    return startUserName;
  }

  public String getStartBodyName() {
    return startBodyName;
  }

  public CoordinatesDto getStartCoordinates() {
    return startCoordinates;
  }

  public String getTargetBodyName() {
    return targetBodyName;
  }

  public CoordinatesDto getTargetCoordinates() {
    return targetCoordinates;
  }

  public Long getPartyId() {
    return partyId;
  }

  public MissionDto getMission() {
    return mission;
  }

  public Map<UnitKindDto, Integer> getUnits() {
    return units;
  }

  public boolean isOwn() {
    return own;
  }

  public FlightEventKindDto getKind() {
    return kind;
  }
}
