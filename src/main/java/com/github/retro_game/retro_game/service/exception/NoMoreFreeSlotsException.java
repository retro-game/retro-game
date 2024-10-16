package com.github.retro_game.retro_game.service.exception;

public class NoMoreFreeSlotsException extends ServiceException {
  public NoMoreFreeSlotsException() {
    super("No more free slots");
  }
}
