package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.Map;

public class FlightEventDto {
  private final long id;
  private final Date at;
  private final long startUserId;
  private final long startBodyId;
  private final CoordinatesDto startCoordinates;

  @Nullable
  private final Long targetUserId;

  @Nullable
  private final Long targetBodyId;

  private final CoordinatesDto targetCoordinates;

  @Nullable
  private final Long partyId;

  private final MissionDto mission;
  private final ResourcesDto resources;
  private final Map<UnitKindDto, Integer> units;
  private final boolean own;
  private final FlightEventKindDto kind;

  public FlightEventDto(long id, Date at, long startUserId, long startBodyId, CoordinatesDto startCoordinates,
                        @Nullable Long targetUserId, @Nullable Long targetBodyId, CoordinatesDto targetCoordinates,
                        @Nullable Long partyId, MissionDto mission, ResourcesDto resources,
                        Map<UnitKindDto, Integer> units, boolean own, FlightEventKindDto kind) {
    this.id = id;
    this.at = at;
    this.startUserId = startUserId;
    this.startBodyId = startBodyId;
    this.startCoordinates = startCoordinates;
    this.targetUserId = targetUserId;
    this.targetBodyId = targetBodyId;
    this.targetCoordinates = targetCoordinates;
    this.partyId = partyId;
    this.mission = mission;
    this.resources = resources;
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

  public long getStartBodyId() {
    return startBodyId;
  }

  public CoordinatesDto getStartCoordinates() {
    return startCoordinates;
  }

  @Nullable
  public Long getTargetUserId() {
    return targetUserId;
  }

  @Nullable
  public Long getTargetBodyId() {
    return targetBodyId;
  }

  public CoordinatesDto getTargetCoordinates() {
    return targetCoordinates;
  }

  @Nullable
  public Long getPartyId() {
    return partyId;
  }

  public MissionDto getMission() {
    return mission;
  }

  public ResourcesDto getResources() {
    return resources;
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
