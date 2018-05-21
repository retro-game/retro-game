package com.github.retro_game.retro_game.service.exception;

public class NotEnoughUnitsException extends ServiceException {
  public NotEnoughUnitsException() {
    super("Not enough units");
  }
}
