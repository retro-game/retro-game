package com.github.retro_game.retro_game.service.exception;

public class UserNotBannedException extends ServiceException {
  public UserNotBannedException() {
    super("User is not banned");
  }
}
