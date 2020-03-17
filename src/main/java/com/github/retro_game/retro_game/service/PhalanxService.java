package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PhalanxFlightEventDto;

import java.util.List;

public interface PhalanxService {
  @Activity(bodies = "#bodyId")
  boolean systemWithinRange(long bodyId, int galaxy, int system);

  @Activity(bodies = "#bodyId")
  List<PhalanxFlightEventDto> scan(long bodyId, int galaxy, int system, int position);
}
