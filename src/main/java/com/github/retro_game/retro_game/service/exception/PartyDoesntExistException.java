package com.github.retro_game.retro_game.service.exception;

public class PartyDoesntExistException extends ServiceException {
  public PartyDoesntExistException() {
    super("Party doesn't exist");
  }
}
