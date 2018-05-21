package com.github.retro_game.retro_game.service.exception;

public class MessageDoesntExistException extends ServiceException {
  public MessageDoesntExistException() {
    super("Message doesn't exist");
  }
}
