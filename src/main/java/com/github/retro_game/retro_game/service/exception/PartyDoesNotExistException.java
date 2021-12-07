package com.github.retro_game.retro_game.service.exception;

public class PartyDoesNotExistException extends ServiceException {
  public PartyDoesNotExistException() {
    super("Party does not exist");
  }
}
