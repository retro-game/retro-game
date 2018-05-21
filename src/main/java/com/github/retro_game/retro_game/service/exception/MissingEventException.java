package com.github.retro_game.retro_game.service.exception;

public class MissingEventException extends ServiceException {
  public MissingEventException() {
    super("Missing task");
  }
}
