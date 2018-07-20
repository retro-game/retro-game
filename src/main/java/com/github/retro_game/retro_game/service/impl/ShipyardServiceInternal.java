package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.Body;
import com.github.retro_game.retro_game.model.entity.UnitKind;
import com.github.retro_game.retro_game.service.ShipyardService;
import io.vavr.Tuple2;

import java.util.Map;

interface ShipyardServiceInternal extends EventHandler, ShipyardService {
  Map<UnitKind, Tuple2<Integer, Integer>> getCurrentAndFutureCounts(Body body);

  void deleteUnitsAndQueue(Body body);
}
