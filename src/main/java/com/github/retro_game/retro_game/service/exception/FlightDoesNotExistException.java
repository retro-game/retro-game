package com.github.retro_game.retro_game.service.exception;

public class FlightDoesNotExistException extends ServiceException {
  public FlightDoesNotExistException() {
    super("Flight does not exist");
  }
}
