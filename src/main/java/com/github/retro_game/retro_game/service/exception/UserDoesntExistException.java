package com.github.retro_game.retro_game.service.exception;

public class UserDoesntExistException extends ServiceException {
  public UserDoesntExistException() {
    super("User doesn't exist");
  }
}
