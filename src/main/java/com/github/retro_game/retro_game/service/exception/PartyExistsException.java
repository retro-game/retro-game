package com.github.retro_game.retro_game.service.exception;

public class PartyExistsException extends ServiceException {
  public PartyExistsException() {
    super("Party already exists");
  }
}
