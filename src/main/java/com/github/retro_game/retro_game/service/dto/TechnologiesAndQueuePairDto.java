package com.github.retro_game.retro_game.service.dto;

import java.util.List;

public class TechnologiesAndQueuePairDto {
  private final List<TechnologyDto> technologies;
  private final List<TechnologyQueueEntryDto> queue;

  public TechnologiesAndQueuePairDto(List<TechnologyDto> technologies, List<TechnologyQueueEntryDto> queue) {
    this.technologies = technologies;
    this.queue = queue;
  }

  public List<TechnologyDto> getTechnologies() {
    return technologies;
  }

  public List<TechnologyQueueEntryDto> getQueue() {
    return queue;
  }
}
