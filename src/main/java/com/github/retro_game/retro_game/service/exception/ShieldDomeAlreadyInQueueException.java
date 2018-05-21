package com.github.retro_game.retro_game.service.exception;

public class ShieldDomeAlreadyInQueueException extends ServiceException {
  public ShieldDomeAlreadyInQueueException() {
    super("The dome is already in queue");
  }
}
