package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.Body;
import com.github.retro_game.retro_game.model.entity.BuildingKind;
import com.github.retro_game.retro_game.model.entity.Resources;
import com.github.retro_game.retro_game.service.BuildingsService;

import java.util.Date;
import java.util.Optional;

interface BuildingsServiceInternal extends BuildingsService, EventHandler {
  Optional<OngoingBuildingDto> getOngoingBuilding(Body body);

  Optional<Date> getOngoingBuildingFinishAt(Body body);

  void deleteBuildingsAndQueue(Body body);

  Resources getCost(BuildingKind kind, int level);

  long getDestructionTime(Resources cost, Body body);
}
