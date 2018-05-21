package com.github.retro_game.retro_game.service.dto;

import java.util.List;

public class UnitsAndQueuePairDto {
  private final List<UnitDto> units;
  private final List<ShipyardQueueEntryDto> queue;

  public UnitsAndQueuePairDto(List<UnitDto> units, List<ShipyardQueueEntryDto> queue) {
    this.units = units;
    this.queue = queue;
  }

  public List<UnitDto> getUnits() {
    return units;
  }

  public List<ShipyardQueueEntryDto> getQueue() {
    return queue;
  }
}
