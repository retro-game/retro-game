package com.github.retro_game.retro_game.service.exception;

public class AllianceDoesNotExistException extends ServiceException {
  public AllianceDoesNotExistException() {
    super("Alliance does not exist");
  }
}
