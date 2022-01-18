package com.github.retro_game.retro_game.service.exception;

public class ReportDoesNotExistException extends ServiceException {
  public ReportDoesNotExistException() {
    super("Report does not exist");
  }
}
