package com.github.retro_game.retro_game.service.exception;

public class NotEnoughEnergyException extends ServiceException {
  public NotEnoughEnergyException() {
    super("Not enough energy");
  }
}
