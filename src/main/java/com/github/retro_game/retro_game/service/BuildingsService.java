package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.BuildingKindDto;
import com.github.retro_game.retro_game.dto.BuildingsAndQueuePairDto;

public interface BuildingsService {
  BuildingsAndQueuePairDto getBuildingsAndQueuePair(long bodyId);

  int getLevel(long bodyId, BuildingKindDto kind);

  void construct(long bodyId, BuildingKindDto kind);

  void destroy(long bodyId, BuildingKindDto kind);

  void moveDown(long bodyId, int sequenceNumber);

  void moveUp(long bodyId, int sequenceNumber);

  void cancel(long bodyId, int sequenceNumber);
}
