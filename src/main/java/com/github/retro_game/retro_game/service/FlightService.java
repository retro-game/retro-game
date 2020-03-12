package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

public interface FlightService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<OverviewFlightEventDto> getOverviewFlightEvents(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<FlightDto> getFlights(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  int getOccupiedFlightSlots(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  int getMaxFlightSlots(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  Map<UnitKindDto, FlyableUnitInfoDto> getFlyableUnits(long bodyId);

  @PreAuthorize("hasPermission(#params.bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#params.bodyId")
  void send(SendFleetParamsDto params);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void sendProbes(long bodyId, CoordinatesDto targetCoordinates, int numProbes);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void sendMissiles(long bodyId, CoordinatesDto targetCoordinates, int numMissiles);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void recall(long bodyId, long flightId);
}
