package com.github.retro_game.retro_game.service.exception;

public class BuildingAlreadyDestroyedException extends ServiceException {
  public BuildingAlreadyDestroyedException() {
    super("Building is already going to be destroyed");
  }
}
