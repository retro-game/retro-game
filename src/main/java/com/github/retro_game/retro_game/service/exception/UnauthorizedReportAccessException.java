package com.github.retro_game.retro_game.service.exception;

public class UnauthorizedReportAccessException extends ServiceException {
  public UnauthorizedReportAccessException() {
    super("Unauthorized report access");
  }
}
