package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DetailsService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  BuildingDetailsDto getBuildingDetails(long bodyId, BuildingKindDto kind);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  TechnologyDetailsDto getTechnologyDetails(long bodyId, TechnologyKindDto kind);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  UnitDetailsDto getUnitDetails(long bodyId, UnitKindDto kind);
}
