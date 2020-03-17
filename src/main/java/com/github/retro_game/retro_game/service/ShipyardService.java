package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.dto.UnitTypeDto;
import com.github.retro_game.retro_game.dto.UnitsAndQueuePairDto;

public interface ShipyardService {
  @Activity(bodies = "#bodyId")
  UnitsAndQueuePairDto getUnitsAndQueuePair(long bodyId, UnitTypeDto type);

  @Activity(bodies = "#bodyId")
  void build(long bodyId, UnitKindDto kind, int count);
}
