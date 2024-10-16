package com.github.retro_game.retro_game.service.exception;

public class UnauthorizedPartyAccessException extends ServiceException {
  public UnauthorizedPartyAccessException() {
    super("Unauthorized party access");
  }
}
