package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.TechnologyTreeDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface TechnologyTreeService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  TechnologyTreeDto getTechnologyTree(long bodyId);
}
