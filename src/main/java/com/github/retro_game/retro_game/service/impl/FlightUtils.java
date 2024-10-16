package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.entity.UnitKind;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

class FlightUtils {
  static EnumMap<UnitKindDto, Integer> convertUnitsWithPositiveCount(Map<UnitKind, Integer> units) {
    return units.entrySet().stream()
        .filter(e -> e.getValue() > 0)
        .collect(Collectors.toMap(
            e -> Converter.convert(e.getKey()),
            Map.Entry::getValue,
            (a, b) -> {
              throw new IllegalStateException();
            },
            () -> new EnumMap<>(UnitKindDto.class)
        ));
  }
}
