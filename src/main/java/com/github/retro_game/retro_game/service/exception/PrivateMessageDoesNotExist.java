package com.github.retro_game.retro_game.service.exception;

public class PrivateMessageDoesNotExist extends ServiceException {
  public PrivateMessageDoesNotExist() {
    super("Private message does not exist");
  }
}
