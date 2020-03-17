package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.JumpGateInfoDto;
import com.github.retro_game.retro_game.dto.UnitKindDto;

import java.util.Map;

public interface JumpGateService {
  @Activity(bodies = "#bodyId")
  JumpGateInfoDto getInfo(long bodyId);

  @Activity(bodies = {"#bodyId", "#targetId"})
  void jump(long bodyId, long targetId, Map<UnitKindDto, Integer> units);
}
