package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.dto.UnitTypeDto;
import com.github.retro_game.retro_game.dto.UnitsAndQueuePairDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ShipyardService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  UnitsAndQueuePairDto getUnitsAndQueuePair(long bodyId, UnitTypeDto type);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void build(long bodyId, UnitKindDto kind, int count);
}
