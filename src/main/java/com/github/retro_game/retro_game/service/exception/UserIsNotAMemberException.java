package com.github.retro_game.retro_game.service.exception;

public class UserIsNotAMemberException extends ServiceException {
  public UserIsNotAMemberException() {
    super("User is not a member");
  }
}
