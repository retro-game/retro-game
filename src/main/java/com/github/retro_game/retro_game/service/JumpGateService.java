package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.JumpGateInfoDto;
import com.github.retro_game.retro_game.dto.UnitKindDto;

import java.util.Map;

public interface JumpGateService {
  JumpGateInfoDto getInfo(long bodyId);

  void jump(long bodyId, long targetId, Map<UnitKindDto, Integer> units);
}
