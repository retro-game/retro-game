package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.TechnologiesAndQueuePairDto;
import com.github.retro_game.retro_game.dto.TechnologyKindDto;

public interface TechnologyService {
  @Activity(bodies = "#bodyId")
  int getLevel(long bodyId, TechnologyKindDto kind);

  @Activity(bodies = "#bodyId")
  TechnologiesAndQueuePairDto getTechnologiesAndQueuePair(long bodyId);

  @Activity(bodies = "#bodyId")
  void research(long bodyId, TechnologyKindDto kind);

  @Activity(bodies = "#bodyId")
  void moveDown(long bodyId, int sequenceNumber);

  @Activity(bodies = "#bodyId")
  void moveUp(long bodyId, int sequenceNumber);

  @Activity(bodies = "#bodyId")
  void cancel(long bodyId, int sequenceNumber);
}
