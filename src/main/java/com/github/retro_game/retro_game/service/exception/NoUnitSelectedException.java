package com.github.retro_game.retro_game.service.exception;

public class NoUnitSelectedException extends ServiceException {
  public NoUnitSelectedException() {
    super("No unit selected");
  }
}
