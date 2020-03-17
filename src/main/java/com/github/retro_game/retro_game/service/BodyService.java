package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import org.springframework.lang.Nullable;

import java.util.List;

public interface BodyService {
  long createHomeworld(int galaxy, int system, int position);

  @Activity(bodies = "#bodyId")
  int getTemperature(long bodyId);

  @Activity(bodies = "#bodyId")
  BodyInfoDto getBodyBasicInfo(long bodyId);

  @Activity(bodies = "#bodyId")
  List<BodyInfoDto> getBodiesBasicInfo(long bodyId);

  @Activity(bodies = "#bodyId")
  BodyTypeAndImagePairDto getBodyTypeAndImagePair(long bodyId);

  // Gets the previous and the next (pointers) bodies relatively to the selected body.
  @Activity(bodies = "#bodyId")
  BodiesPointersDto getBodiesPointers(long bodyId);

  @Activity(bodies = "#bodyId")
  OverviewBodiesDto getOverviewBodies(long bodyId);

  @Activity(bodies = "#bodyId")
  EmpireDto getEmpire(long bodyId, @Nullable Integer galaxy, @Nullable Integer system, @Nullable Integer position,
                      @Nullable CoordinatesKindDto kind);

  @Activity(bodies = "#bodyId")
  ResourcesDto getResources(long bodyId);

  @Activity(bodies = "#bodyId")
  ProductionDto getProduction(long bodyId);

  @Activity(bodies = "#bodyId")
  ProductionItemsDto getProductionItems(long bodyId);

  @Activity(bodies = "#bodyId")
  ProductionFactorsDto getProductionFactors(long bodyId);

  @Activity(bodies = "#bodyId")
  void setProductionFactors(long bodyId, ProductionFactorsDto factors);

  @Activity(bodies = "#bodyId")
  ResourcesDto getCapacity(long bodyId);

  @Activity(bodies = "#bodyId")
  void rename(long bodyId, String name);

  @Activity(bodies = "#bodyId")
  void setImage(long bodyId, int image);

  @Activity(bodies = "#bodyId")
  long abandonPlanet(long bodyId, String password);
}
