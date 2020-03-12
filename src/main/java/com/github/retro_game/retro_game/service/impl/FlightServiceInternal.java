package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.service.FlightService;
import com.github.retro_game.retro_game.service.dto.PhalanxFlightEventDto;

import java.util.Collection;
import java.util.List;

interface FlightServiceInternal extends EventHandler, FlightService {
  boolean existsByStartOrTargetIn(Collection<Body> bodies);

  boolean existsByUser(User user);

  List<PhalanxFlightEventDto> getPhalanxFlightEvents(int galaxy, int system, int position);
}
