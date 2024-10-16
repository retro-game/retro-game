package com.github.retro_game.retro_game.service.exception;

public class CannotDeleteLastPlanetException extends ServiceException {
  public CannotDeleteLastPlanetException() {
    super("Cannot delete the last planet");
  }
}
