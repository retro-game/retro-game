package com.github.retro_game.retro_game.service.exception;

public class UserIsAlreadyApplyingToAllianceException extends ServiceException {
  public UserIsAlreadyApplyingToAllianceException() {
    super("User is already applying to an alliance");
  }
}
