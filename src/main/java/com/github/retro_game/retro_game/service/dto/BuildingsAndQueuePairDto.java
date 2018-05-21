package com.github.retro_game.retro_game.service.dto;

import java.util.List;

public class BuildingsAndQueuePairDto {
  private final List<BuildingDto> buildings;
  private final List<BuildingQueueEntryDto> queue;

  public BuildingsAndQueuePairDto(List<BuildingDto> buildings, List<BuildingQueueEntryDto> queue) {
    this.buildings = buildings;
    this.queue = queue;
  }

  public List<BuildingDto> getBuildings() {
    return buildings;
  }

  public List<BuildingQueueEntryDto> getQueue() {
    return queue;
  }
}
