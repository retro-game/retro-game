package com.github.retro_game.retro_game.service.exception;

public class BodyDoesNotExistException extends ServiceException {
  public BodyDoesNotExistException() {
    super("Body does not exist");
  }
}
