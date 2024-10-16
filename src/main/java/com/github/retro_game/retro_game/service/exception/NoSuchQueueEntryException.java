package com.github.retro_game.retro_game.service.exception;

public class NoSuchQueueEntryException extends ServiceException {
  public NoSuchQueueEntryException() {
    super("No such queue entry");
  }
}
