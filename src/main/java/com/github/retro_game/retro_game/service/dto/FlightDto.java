package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.Map;

public class FlightDto {
  private final long id;
  private final String startBodyName;
  private final CoordinatesDto startCoordinates;
  private final String targetBodyName;
  private final CoordinatesDto targetCoordinates;
  private final Long partyId;
  private final MissionDto mission;
  private final Date departureAt;
  private final Date arrivalAt;
  private final Date returnAt;
  private final ResourcesDto resources;
  private final Map<UnitKindDto, Integer> units;
  private final boolean recallable;
  private final boolean partyCreatable;

  public FlightDto(long id, String startBodyName, CoordinatesDto startCoordinates, @Nullable String targetBodyName,
                   CoordinatesDto targetCoordinates, @Nullable Long partyId, MissionDto mission, Date departureAt,
                   @Nullable Date arrivalAt, Date returnAt, ResourcesDto resources, Map<UnitKindDto, Integer> units,
                   boolean recallable, boolean partyCreatable) {
    this.id = id;
    this.startBodyName = startBodyName;
    this.startCoordinates = startCoordinates;
    this.targetBodyName = targetBodyName;
    this.targetCoordinates = targetCoordinates;
    this.partyId = partyId;
    this.mission = mission;
    this.departureAt = departureAt;
    this.arrivalAt = arrivalAt;
    this.returnAt = returnAt;
    this.resources = resources;
    this.units = units;
    this.recallable = recallable;
    this.partyCreatable = partyCreatable;
  }

  public long getId() {
    return id;
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

  public Date getDepartureAt() {
    return departureAt;
  }

  public Date getArrivalAt() {
    return arrivalAt;
  }

  public Date getReturnAt() {
    return returnAt;
  }

  public ResourcesDto getResources() {
    return resources;
  }

  public Map<UnitKindDto, Integer> getUnits() {
    return units;
  }

  public boolean isRecallable() {
    return recallable;
  }

  public boolean isPartyCreatable() {
    return partyCreatable;
  }
}
