package com.github.retro_game.retro_game.service.exception;

public abstract class ServiceException extends RuntimeException {
  public ServiceException(String message) {
    super(message);
  }
}
