package com.github.retro_game.retro_game.service.exception;

public class AllianceTagAlreadyExistsException extends ServiceException {
  public AllianceTagAlreadyExistsException() {
    super("Alliance tag already exists");
  }
}
