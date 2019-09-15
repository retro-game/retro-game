package com.github.retro_game.retro_game.service.exception;

public class UnauthorizedPrivateMessageAccessException extends ServiceException {
  public UnauthorizedPrivateMessageAccessException() {
    super("Unauthorized private message access");
  }
}
