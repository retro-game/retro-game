package com.github.retro_game.retro_game.service.exception;

public class DebrisFieldDoesntExistException extends ServiceException {
  public DebrisFieldDoesntExistException() {
    super("Debris field doesn't exist");
  }
}
