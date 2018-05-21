package com.github.retro_game.retro_game.service.exception;

public class FlightDoesntExistException extends ServiceException {
  public FlightDoesntExistException() {
    super("Flight doesn't exist");
  }
}
