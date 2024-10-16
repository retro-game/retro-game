package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.dto.UnitTypeDto;
import com.github.retro_game.retro_game.dto.UnitsAndQueuePairDto;
import com.github.retro_game.retro_game.entity.Body;

import java.util.Date;

public interface ShipyardService {
  UnitsAndQueuePairDto getUnitsAndQueuePair(long bodyId, UnitTypeDto type);

  void build(long bodyId, UnitKindDto kind, int count);

  void update(Body body, Date at);
}
