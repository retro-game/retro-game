package com.github.retro_game.retro_game.service.exception;

public class NoColonyShipSelectedException extends ServiceException {
  public NoColonyShipSelectedException() {
    super("No colony ship selected");
  }
}
