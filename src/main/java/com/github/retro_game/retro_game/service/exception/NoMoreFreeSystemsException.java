package com.github.retro_game.retro_game.service.exception;

public class NoMoreFreeSystemsException extends RuntimeException {
  public NoMoreFreeSystemsException() {
    super("No more free systems");
  }
}
