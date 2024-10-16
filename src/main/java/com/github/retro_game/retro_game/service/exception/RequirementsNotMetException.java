package com.github.retro_game.retro_game.service.exception;

public class RequirementsNotMetException extends ServiceException {
  public RequirementsNotMetException() {
    super("Requirements not met");
  }
}
