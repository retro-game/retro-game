package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.GalaxySlotDto;

import java.util.Map;

public interface GalaxyService {
  // A player needs the galaxy view when she/he wants to create the homeworld.
  Map<Integer, GalaxySlotDto> getSlots(int galaxy, int system);

  @Activity(bodies = "#bodyId")
  Map<Integer, GalaxySlotDto> getSlots(long bodyId, int galaxy, int system);
}
