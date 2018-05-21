package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class OtherReportDto {
  private final long id;
  private final Date at;
  private final OtherReportKindDto kind;
  private final CoordinatesDto startCoordinates;
  private final CoordinatesDto targetCoordinates;
  private final ResourcesDto resources;
  private final Double param;

  public OtherReportDto(long id, Date at, OtherReportKindDto kind, CoordinatesDto startCoordinates,
                        CoordinatesDto targetCoordinates, @Nullable ResourcesDto resources, @Nullable Double param) {
    this.id = id;
    this.at = at;
    this.kind = kind;
    this.startCoordinates = startCoordinates;
    this.targetCoordinates = targetCoordinates;
    this.resources = resources;
    this.param = param;
  }

  public long getId() {
    return id;
  }

  public Date getAt() {
    return at;
  }

  public OtherReportKindDto getKind() {
    return kind;
  }

  public CoordinatesDto getStartCoordinates() {
    return startCoordinates;
  }

  public CoordinatesDto getTargetCoordinates() {
    return targetCoordinates;
  }

  public ResourcesDto getResources() {
    return resources;
  }

  public Double getParam() {
    return param;
  }
}
