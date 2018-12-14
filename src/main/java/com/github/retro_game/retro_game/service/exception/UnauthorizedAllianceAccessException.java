package com.github.retro_game.retro_game.service.exception;

public class UnauthorizedAllianceAccessException extends ServiceException {
  public UnauthorizedAllianceAccessException() {
    super("Unauthorized alliance access");
  }
}
