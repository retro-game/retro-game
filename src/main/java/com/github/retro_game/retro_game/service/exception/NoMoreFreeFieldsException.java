package com.github.retro_game.retro_game.service.exception;

public class NoMoreFreeFieldsException extends ServiceException {
  public NoMoreFreeFieldsException() {
    super("No more free fields");
  }
}
