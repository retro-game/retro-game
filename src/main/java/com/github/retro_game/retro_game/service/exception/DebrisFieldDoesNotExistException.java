package com.github.retro_game.retro_game.service.exception;

public class DebrisFieldDoesNotExistException extends ServiceException {
  public DebrisFieldDoesNotExistException() {
    super("Debris field does not exist");
  }
}
