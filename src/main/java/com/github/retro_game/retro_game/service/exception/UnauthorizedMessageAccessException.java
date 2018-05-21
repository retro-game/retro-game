package com.github.retro_game.retro_game.service.exception;

public class UnauthorizedMessageAccessException extends ServiceException {
  public UnauthorizedMessageAccessException() {
    super("Unauthorized message access");
  }
}
