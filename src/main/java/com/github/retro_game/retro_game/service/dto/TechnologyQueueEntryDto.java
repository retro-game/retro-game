package com.github.retro_game.retro_game.service.dto;

import java.util.Date;

public class TechnologyQueueEntryDto {
  private final TechnologyKindDto kind;
  private final int sequence;
  private final int level;
  private final ResourcesDto cost;
  private final int requiredEnergy;
  private final long bodyId;
  private final String bodyName;
  private final CoordinatesDto bodyCoordinates;
  private final int effectiveLabLevel;
  private final Date finishAt;
  private final long requiredTime;
  private final boolean downMovable;
  private final boolean upMovable;
  private final boolean cancelable;

  public TechnologyQueueEntryDto(TechnologyKindDto kind, int sequence, int level, ResourcesDto cost, int requiredEnergy,
                                 long bodyId, String bodyName, CoordinatesDto bodyCoordinates, int effectiveLabLevel,
                                 Date finishAt, long requiredTime, boolean downMovable, boolean upMovable,
                                 boolean cancelable) {
    this.kind = kind;
    this.sequence = sequence;
    this.level = level;
    this.cost = cost;
    this.requiredEnergy = requiredEnergy;
    this.bodyId = bodyId;
    this.bodyName = bodyName;
    this.bodyCoordinates = bodyCoordinates;
    this.effectiveLabLevel = effectiveLabLevel;
    this.finishAt = finishAt;
    this.requiredTime = requiredTime;
    this.downMovable = downMovable;
    this.upMovable = upMovable;
    this.cancelable = cancelable;
  }

  public TechnologyKindDto getKind() {
    return kind;
  }

  public int getSequence() {
    return sequence;
  }

  public int getLevel() {
    return level;
  }

  public ResourcesDto getCost() {
    return cost;
  }

  public int getRequiredEnergy() {
    return requiredEnergy;
  }

  public long getBodyId() {
    return bodyId;
  }

  public String getBodyName() {
    return bodyName;
  }

  public CoordinatesDto getBodyCoordinates() {
    return bodyCoordinates;
  }

  public int getEffectiveLabLevel() {
    return effectiveLabLevel;
  }

  public Date getFinishAt() {
    return finishAt;
  }

  public long getRequiredTime() {
    return requiredTime;
  }

  public boolean isDownMovable() {
    return downMovable;
  }

  public boolean isUpMovable() {
    return upMovable;
  }

  public boolean isCancelable() {
    return cancelable;
  }
}
