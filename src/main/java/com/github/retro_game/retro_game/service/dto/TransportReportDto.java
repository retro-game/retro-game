package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class TransportReportDto {
  private final long id;
  private final Date at;
  private final TransportKindDto kind;
  private final Long partnerId;
  private final String partnerName;
  private final CoordinatesDto startCoordinates;
  private final CoordinatesDto targetCoordinates;
  private final ResourcesDto resources;

  public TransportReportDto(long id, Date at, TransportKindDto kind, @Nullable Long partnerId, String partnerName,
                            CoordinatesDto startCoordinates, CoordinatesDto targetCoordinates, ResourcesDto resources) {
    this.id = id;
    this.at = at;
    this.kind = kind;
    this.partnerId = partnerId;
    this.partnerName = partnerName;
    this.startCoordinates = startCoordinates;
    this.targetCoordinates = targetCoordinates;
    this.resources = resources;
  }

  public long getId() {
    return id;
  }

  public Date getAt() {
    return at;
  }

  public TransportKindDto getKind() {
    return kind;
  }

  public Long getPartnerId() {
    return partnerId;
  }

  public String getPartnerName() {
    return partnerName;
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
}
