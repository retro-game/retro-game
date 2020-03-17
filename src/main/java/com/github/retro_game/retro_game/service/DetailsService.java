package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;

public interface DetailsService {
  @Activity(bodies = "#bodyId")
  BuildingDetailsDto getBuildingDetails(long bodyId, BuildingKindDto kind);

  @Activity(bodies = "#bodyId")
  TechnologyDetailsDto getTechnologyDetails(long bodyId, TechnologyKindDto kind);

  @Activity(bodies = "#bodyId")
  UnitDetailsDto getUnitDetails(long bodyId, UnitKindDto kind);
}
