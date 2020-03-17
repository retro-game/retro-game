package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.BuildingKindDto;
import com.github.retro_game.retro_game.dto.BuildingsAndQueuePairDto;

public interface BuildingsService {
  @Activity(bodies = "#bodyId")
  BuildingsAndQueuePairDto getBuildingsAndQueuePair(long bodyId);

  @Activity(bodies = "#bodyId")
  void construct(long bodyId, BuildingKindDto kind);

  @Activity(bodies = "#bodyId")
  void destroy(long bodyId, BuildingKindDto kind);

  @Activity(bodies = "#bodyId")
  void moveDown(long bodyId, int sequenceNumber);

  @Activity(bodies = "#bodyId")
  void moveUp(long bodyId, int sequenceNumber);

  @Activity(bodies = "#bodyId")
  void cancel(long bodyId, int sequenceNumber);
}
