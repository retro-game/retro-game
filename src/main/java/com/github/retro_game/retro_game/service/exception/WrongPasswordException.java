package com.github.retro_game.retro_game.service.exception;

public class WrongPasswordException extends ServiceException {
  public WrongPasswordException() {
    super("Wrong password");
  }
}
