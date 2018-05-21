package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface BodyService {
  @PreAuthorize("isAuthenticated()")
  long createHomeworld(int galaxy, int system, int position);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  BodyBasicInfoDto getBodyBasicInfo(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<BodyBasicInfoDto> getBodiesBasicInfo(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  BodyTypeAndImagePairDto getBodyTypeAndImagePair(long bodyId);

  // Gets the previous and the next (pointers) bodies relatively to the selected body.
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  BodiesPointersDto getBodiesPointers(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  OverviewBodiesDto getOverviewBodies(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  ResourcesDto getResources(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  ProductionDto getProduction(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  ProductionItemsDto getProductionItems(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  ProductionFactorsDto getProductionFactors(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void setProductionFactors(long bodyId, ProductionFactorsDto factors);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  ResourcesDto getCapacity(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void rename(long bodyId, String name);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void setImage(long bodyId, int image);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  long abandonPlanet(long bodyId, String password);
}
