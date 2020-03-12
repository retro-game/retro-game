package com.github.retro_game.retro_game.dto;

import java.util.Date;

public class BuildingQueueEntryDto {
  private final BuildingKindDto kind;
  private final int sequence;
  private final int levelFrom;
  private final int levelTo;
  private final ResourcesDto cost;
  private final int requiredEnergy;
  private final Date finishAt;
  private final long requiredTime;
  private final boolean downMovable;
  private final boolean upMovable;
  private final boolean cancelable;

  public BuildingQueueEntryDto(BuildingKindDto kind, int sequence, int levelFrom, int levelTo, ResourcesDto cost,
                               int requiredEnergy, Date finishAt, long requiredTime, boolean downMovable,
                               boolean upMovable, boolean cancelable) {
    this.kind = kind;
    this.sequence = sequence;
    this.levelFrom = levelFrom;
    this.levelTo = levelTo;
    this.cost = cost;
    this.requiredEnergy = requiredEnergy;
    this.finishAt = finishAt;
    this.requiredTime = requiredTime;
    this.downMovable = downMovable;
    this.upMovable = upMovable;
    this.cancelable = cancelable;
  }

  public BuildingKindDto getKind() {
    return kind;
  }

  public int getSequence() {
    return sequence;
  }

  public int getLevelFrom() {
    return levelFrom;
  }

  public int getLevelTo() {
    return levelTo;
  }

  public ResourcesDto getCost() {
    return cost;
  }

  public int getRequiredEnergy() {
    return requiredEnergy;
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
