package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.UnitKindDto;

import java.util.Map;

public interface RapidFireTableService {
  Map<UnitKindDto, Map<UnitKindDto, Integer>> getRapidFireTable(long bodyId);
}
