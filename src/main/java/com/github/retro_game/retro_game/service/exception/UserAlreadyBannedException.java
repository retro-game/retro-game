package com.github.retro_game.retro_game.service.exception;

public class UserAlreadyBannedException extends ServiceException {
  public UserAlreadyBannedException() {
    super("User is already banned, you must unban the user first");
  }
}
