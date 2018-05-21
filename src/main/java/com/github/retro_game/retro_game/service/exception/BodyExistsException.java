package com.github.retro_game.retro_game.service.exception;

public class BodyExistsException extends ServiceException {
  public BodyExistsException() {
    super("Body already exists");
  }
}
