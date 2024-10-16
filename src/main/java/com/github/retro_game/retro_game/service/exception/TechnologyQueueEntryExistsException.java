package com.github.retro_game.retro_game.service.exception;

public class TechnologyQueueEntryExistsException extends ServiceException {
  public TechnologyQueueEntryExistsException() {
    super("A technology queue entry exists, please cancel it first and then abandonPlanet the planet");
  }
}
