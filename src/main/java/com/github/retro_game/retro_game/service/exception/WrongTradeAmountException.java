package com.github.retro_game.retro_game.service.exception;

public class WrongTradeAmountException extends ServiceException {
  public WrongTradeAmountException() {
    super("Wrong trade amount");
  }
}
