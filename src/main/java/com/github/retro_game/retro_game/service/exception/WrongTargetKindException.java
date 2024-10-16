package com.github.retro_game.retro_game.service.exception;

public class WrongTargetKindException extends ServiceException {
  public WrongTargetKindException() {
    super("Wrong target kind for this mission");
  }
}
