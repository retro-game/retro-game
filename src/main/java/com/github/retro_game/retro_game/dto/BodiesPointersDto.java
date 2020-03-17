package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

public class BodiesPointersDto {
  private final BodyInfoDto previous;
  private final BodyInfoDto next;

  public BodiesPointersDto(@Nullable BodyInfoDto previous, @Nullable BodyInfoDto next) {
    this.previous = previous;
    this.next = next;
  }

  public BodyInfoDto getPrevious() {
    return previous;
  }

  public BodyInfoDto getNext() {
    return next;
  }
}
