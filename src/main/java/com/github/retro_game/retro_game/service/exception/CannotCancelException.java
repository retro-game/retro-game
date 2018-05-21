package com.github.retro_game.retro_game.service.exception;

public class CannotCancelException extends ServiceException {
  public CannotCancelException() {
    super("Cannot cancel");
  }
}
