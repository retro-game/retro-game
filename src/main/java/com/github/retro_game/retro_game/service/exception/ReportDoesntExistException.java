package com.github.retro_game.retro_game.service.exception;

public class ReportDoesntExistException extends ServiceException {
  public ReportDoesntExistException() {
    super("Report doesn't exist");
  }
}
