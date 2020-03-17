package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;

import java.util.List;
import java.util.Map;

public interface FlightService {
  @Activity(bodies = "#bodyId")
  List<OverviewFlightEventDto> getOverviewFlightEvents(long bodyId);

  @Activity(bodies = "#bodyId")
  List<FlightDto> getFlights(long bodyId);

  @Activity(bodies = "#bodyId")
  int getOccupiedFlightSlots(long bodyId);

  @Activity(bodies = "#bodyId")
  int getMaxFlightSlots(long bodyId);

  @Activity(bodies = "#bodyId")
  Map<UnitKindDto, FlyableUnitInfoDto> getFlyableUnits(long bodyId);

  @Activity(bodies = "#params.bodyId")
  void send(SendFleetParamsDto params);

  @Activity(bodies = "#bodyId")
  void sendProbes(long bodyId, CoordinatesDto targetCoordinates, int numProbes);

  @Activity(bodies = "#bodyId")
  void sendMissiles(long bodyId, CoordinatesDto targetCoordinates, int numMissiles);

  @Activity(bodies = "#bodyId")
  void recall(long bodyId, long flightId);
}
