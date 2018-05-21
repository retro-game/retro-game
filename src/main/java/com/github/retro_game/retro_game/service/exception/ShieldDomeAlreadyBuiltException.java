package com.github.retro_game.retro_game.service.exception;

public class ShieldDomeAlreadyBuiltException extends ServiceException {
  public ShieldDomeAlreadyBuiltException() {
    super("The dome is already built");
  }
}
