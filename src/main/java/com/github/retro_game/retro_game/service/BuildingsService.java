package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.BuildingKindDto;
import com.github.retro_game.retro_game.dto.BuildingsAndQueuePairDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface BuildingsService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  BuildingsAndQueuePairDto getBuildingsAndQueuePair(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void construct(long bodyId, BuildingKindDto kind);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void destroy(long bodyId, BuildingKindDto kind);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void moveDown(long bodyId, int sequenceNumber);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void moveUp(long bodyId, int sequenceNumber);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void cancel(long bodyId, int sequenceNumber);
}
