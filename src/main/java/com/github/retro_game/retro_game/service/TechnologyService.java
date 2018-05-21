package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.TechnologiesAndQueuePairDto;
import com.github.retro_game.retro_game.service.dto.TechnologyKindDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface TechnologyService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  TechnologiesAndQueuePairDto getTechnologiesAndQueuePair(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void research(long bodyId, TechnologyKindDto kind);

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
