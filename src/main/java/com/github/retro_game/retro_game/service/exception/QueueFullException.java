package com.github.retro_game.retro_game.service.exception;

public class QueueFullException extends ServiceException {
  public QueueFullException() {
    super("Queue full");
  }
}
