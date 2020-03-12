package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

public interface RapidFireTableService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  Map<UnitKindDto, Map<UnitKindDto, Integer>> getRapidFireTable(long bodyId);
}
