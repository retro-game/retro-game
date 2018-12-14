package com.github.retro_game.retro_game.service.exception;

public class AllianceNameAlreadyExistsException extends ServiceException {
  public AllianceNameAlreadyExistsException() {
    super("Alliance name already exists");
  }
}
