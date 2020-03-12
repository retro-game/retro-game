package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.JumpGateInfoDto;
import com.github.retro_game.retro_game.dto.UnitKindDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

public interface JumpGateService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  JumpGateInfoDto getInfo(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY') and hasPermission(#targetId, 'ACCESS_BODY')")
  @Activity(bodies = {"#bodyId", "#targetId"})
  void jump(long bodyId, long targetId, Map<UnitKindDto, Integer> units);
}
