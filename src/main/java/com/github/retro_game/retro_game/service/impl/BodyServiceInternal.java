package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.ProductionDto;
import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.service.BodyService;

import java.util.Date;
import java.util.Map;

interface BodyServiceInternal extends BodyService {
  // Returns body with updated resources & shipyard.
  Body getUpdated(long bodyId);

  int getUsedFields(Body body);

  int getMaxFields(Body body);

  int getMaxFields(Body body, Map<BuildingKind, Integer> buildings);

  void updateResourcesAndShipyard(Body body, Date at);

  ProductionDto getProduction(Body body);

  void destroyMoon(Body moon);
}
