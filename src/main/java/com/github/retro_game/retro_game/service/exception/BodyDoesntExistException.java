package com.github.retro_game.retro_game.service.exception;

public class BodyDoesntExistException extends ServiceException {
  public BodyDoesntExistException() {
    super("Body doesn't exist");
  }
}
