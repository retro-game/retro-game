package com.github.retro_game.retro_game.service.exception;

public class FlightsExistException extends ServiceException {
  public FlightsExistException() {
    super("Flights targeting this body or its moon exist");
  }
}
