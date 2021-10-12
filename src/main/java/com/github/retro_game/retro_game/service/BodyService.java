package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.User;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

public interface BodyService {
  long createHomeworld(int galaxy, int system, int position);

  Body createColony(User user, Coordinates coordinates, Date at);

  Body createMoon(User user, Coordinates coordinates, Date at, double chance);

  int getTemperature(long bodyId);

  BodyInfoDto getBodyBasicInfo(long bodyId);

  List<BodyInfoDto> getBodiesBasicInfo(long bodyId);

  BodyTypeAndImagePairDto getBodyTypeAndImagePair(long bodyId);

  // Gets the previous and the next (pointers) bodies relatively to the selected body.
  BodiesPointersDto getBodiesPointers(long bodyId);

  OverviewBodiesDto getOverviewBodies(long bodyId);

  EmpireDto getEmpire(long bodyId, @Nullable Integer galaxy, @Nullable Integer system, @Nullable Integer position,
                      @Nullable CoordinatesKindDto kind);

  ResourcesDto getResources(long bodyId);

  ProductionDto getProduction(long bodyId);

  ProductionItemsDto getProductionItems(long bodyId);

  ProductionFactorsDto getProductionFactors(long bodyId);

  void setProductionFactors(long bodyId, ProductionFactorsDto factors);

  ResourcesDto getCapacity(long bodyId);

  void rename(long bodyId, String name);

  void setImage(long bodyId, int image);

  long abandonPlanet(long bodyId, String password);
}
