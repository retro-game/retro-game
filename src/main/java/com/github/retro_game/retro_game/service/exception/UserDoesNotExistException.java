package com.github.retro_game.retro_game.service.exception;

public class UserDoesNotExistException extends ServiceException {
  public UserDoesNotExistException() {
    super("User does not exist");
  }
}
