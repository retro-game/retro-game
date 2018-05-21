package com.github.retro_game.retro_game.service.exception;

public class UnauthorizedFlightAccessException extends ServiceException {
  public UnauthorizedFlightAccessException() {
    super("Unauthorized flight access");
  }
}
