package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.TechnologyTreeDto;

public interface TechnologyTreeService {
  @Activity(bodies = "#bodyId")
  TechnologyTreeDto getTechnologyTree(long bodyId);
}
