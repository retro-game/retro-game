package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.TechnologiesAndQueuePairDto;
import com.github.retro_game.retro_game.dto.TechnologyKindDto;

public interface TechnologyService {
  TechnologiesAndQueuePairDto getTechnologiesAndQueuePair(long bodyId);

  void research(long bodyId, TechnologyKindDto kind);

  void moveDown(long bodyId, int sequenceNumber);

  void moveUp(long bodyId, int sequenceNumber);

  void cancel(long bodyId, int sequenceNumber);
}
