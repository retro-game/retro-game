package com.github.retro_game.retro_game.service.exception;

public class HomeworldExistsException extends ServiceException {
  public HomeworldExistsException() {
    super("Homeworld already exists");
  }
}
