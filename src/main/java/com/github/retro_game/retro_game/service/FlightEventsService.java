package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.FlightEventDto;

import java.util.List;

public interface FlightEventsService {
  List<FlightEventDto> getOverviewFlightEvents(long bodyId);

  List<FlightEventDto> getPhalanxFlightEvents(int galaxy, int system, int position);
}
