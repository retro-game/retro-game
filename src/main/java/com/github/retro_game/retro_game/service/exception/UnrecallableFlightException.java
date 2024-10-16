package com.github.retro_game.retro_game.service.exception;

public class UnrecallableFlightException extends ServiceException {
  public UnrecallableFlightException() {
    super("This flight is unrecallable now");
  }
}
