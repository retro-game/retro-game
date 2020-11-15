package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.service.FlightService;

import java.util.Collection;

interface FlightServiceInternal extends EventHandler, FlightService {
  boolean existsByStartOrTargetIn(Collection<Body> bodies);

  boolean existsByUser(User user);
}
