package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

public class BodiesPointersDto {
  private final BodyBasicInfoDto previous;
  private final BodyBasicInfoDto next;

  public BodiesPointersDto(@Nullable BodyBasicInfoDto previous, @Nullable BodyBasicInfoDto next) {
    this.previous = previous;
    this.next = next;
  }

  public BodyBasicInfoDto getPrevious() {
    return previous;
  }

  public BodyBasicInfoDto getNext() {
    return next;
  }
}
