package com.github.retro_game.retro_game.dto;

import java.util.Date;

public class ShipyardQueueEntryDto {
  private final UnitKindDto kind;
  private final int count;
  private final int sequence;
  private final ResourcesDto cost;
  private final Date finishAt;
  private final long requiredTime;

  public ShipyardQueueEntryDto(UnitKindDto kind, int count, int sequence, ResourcesDto cost, Date finishAt,
                               long requiredTime) {
    this.kind = kind;
    this.count = count;
    this.sequence = sequence;
    this.cost = cost;
    this.finishAt = finishAt;
    this.requiredTime = requiredTime;
  }

  public UnitKindDto getKind() {
    return kind;
  }

  public int getCount() {
    return count;
  }

  public int getSequence() {
    return sequence;
  }

  public ResourcesDto getCost() {
    return cost;
  }

  public Date getFinishAt() {
    return finishAt;
  }

  public long getRequiredTime() {
    return requiredTime;
  }
}
