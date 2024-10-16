package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.User;

import java.util.Date;

public interface BodyCreationService {
  Body createHomeworld(int galaxy, int system, int position);

  Body createHomeworldAtRandomCoordinates();

  Body createColony(User user, Coordinates coordinates, Date at);

  Body createMoon(User user, Coordinates coordinates, Date at, double chance);
}
