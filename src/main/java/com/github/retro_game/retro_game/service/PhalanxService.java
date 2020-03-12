package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PhalanxFlightEventDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface PhalanxService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  boolean systemWithinRange(long bodyId, int galaxy, int system);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<PhalanxFlightEventDto> scan(long bodyId, int galaxy, int system, int position);
}
