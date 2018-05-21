package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.UnitKindDto;

import java.util.Map;

public interface FormatterService {
  String formatTime(long t);

  String prefixedNumber(long i);

  String formatUnits(Map<UnitKindDto, Integer> units);
}
