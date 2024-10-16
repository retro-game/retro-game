package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.CoordinatesDto;

import java.util.function.Function;

class BodyKeyExtractors<T> {
  private final Function<? super T, Long> idExtractor;
  private final Function<? super T, ? extends CoordinatesDto> coordinatesExtractor;
  private final Function<? super T, String> nameExtractor;

  BodyKeyExtractors(Function<? super T, Long> idExtractor,
                    Function<? super T, ? extends CoordinatesDto> coordinatesExtractor,
                    Function<? super T, String> nameExtractor) {
    this.idExtractor = idExtractor;
    this.coordinatesExtractor = coordinatesExtractor;
    this.nameExtractor = nameExtractor;
  }

  Function<? super T, Long> getIdExtractor() {
    return idExtractor;
  }

  Function<? super T, ? extends CoordinatesDto> getCoordinatesExtractor() {
    return coordinatesExtractor;
  }

  Function<? super T, String> getNameExtractor() {
    return nameExtractor;
  }
}
