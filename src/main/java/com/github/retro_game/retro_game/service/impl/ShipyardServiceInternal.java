package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.Body;
import com.github.retro_game.retro_game.service.ShipyardService;

interface ShipyardServiceInternal extends EventHandler, ShipyardService {
  void deleteUnitsAndQueue(Body body);
}
