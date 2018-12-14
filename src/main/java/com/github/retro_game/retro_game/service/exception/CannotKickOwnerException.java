package com.github.retro_game.retro_game.service.exception;

public class CannotKickOwnerException extends ServiceException {
  public CannotKickOwnerException() {
    super("Cannot kick the owner");
  }
}
