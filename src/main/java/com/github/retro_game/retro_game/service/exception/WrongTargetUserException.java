package com.github.retro_game.retro_game.service.exception;

public class WrongTargetUserException extends ServiceException {
  public WrongTargetUserException() {
    super("Wrong target user for this mission");
  }
}
