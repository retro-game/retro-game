package com.github.retro_game.retro_game.service.exception;

public class OwnerCannotLeaveException extends ServiceException {
  public OwnerCannotLeaveException() {
    super("The owner cannot leave");
  }
}
