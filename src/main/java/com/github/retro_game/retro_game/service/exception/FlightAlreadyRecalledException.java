package com.github.retro_game.retro_game.service.exception;

public class FlightAlreadyRecalledException extends ServiceException {
  public FlightAlreadyRecalledException() {
    super("This flight is already recalled");
  }
}
