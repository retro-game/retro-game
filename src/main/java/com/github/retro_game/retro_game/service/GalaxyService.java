package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.GalaxySlotDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

public interface GalaxyService {
  // A player needs the galaxy view when she/he wants to create the homeworld.
  @PreAuthorize("isAuthenticated()")
  Map<Integer, GalaxySlotDto> getSlots(int galaxy, int system);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  Map<Integer, GalaxySlotDto> getSlots(long bodyId, int galaxy, int system);
}
