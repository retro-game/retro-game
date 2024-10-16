package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.UnitKindDto;

import java.util.Map;

public interface FormatterService {
  String formatTime(long s);

  String formatTimeMs(long ms);

  String prefixedNumber(long i);

  String formatUnits(Map<UnitKindDto, Integer> units);
}
