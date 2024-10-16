package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;

public interface DetailsService {
  BuildingDetailsDto getBuildingDetails(long bodyId, BuildingKindDto kind);

  TechnologyDetailsDto getTechnologyDetails(long bodyId, TechnologyKindDto kind);

  UnitDetailsDto getUnitDetails(long bodyId, UnitKindDto kind);
}
