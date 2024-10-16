package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.FlightEventDto;
import com.github.retro_game.retro_game.dto.FlightEventKindDto;
import com.github.retro_game.retro_game.dto.MissionDto;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.repository.FlightViewRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.FlightEventsService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
class FlightEventsServiceImpl implements FlightEventsService {
  private final FlightViewRepository flightViewRepository;
  private final UserRepository userRepository;

  FlightEventsServiceImpl(FlightViewRepository flightViewRepository, UserRepository userRepository) {
    this.flightViewRepository = flightViewRepository;
    this.userRepository = userRepository;
  }

  @Override
  public List<FlightEventDto> getOverviewFlightEvents(long bodyId) {
    var userId = CustomUser.getCurrentUserId();
    var user = userRepository.getOne(userId);

    var partiesIds = user.getParties().stream().map(Party::getId).collect(Collectors.toSet());
    var flights = flightViewRepository.findAllByStartUserIdOrTargetUserIdOrPartyIdIn(userId, userId, partiesIds);

    var now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));

    Predicate<FlightView> arrivingPredicate = (FlightView flight) -> true;
    Predicate<FlightView> holdingPredicate = (FlightView flight) -> true;

    // Foreign flights that are returning need to be filtered out, thus we show only current user's returning fleets.
    Predicate<FlightView> returningPredicate = (FlightView flight) -> flight.getStartUserId() == userId;

    return createFlightEvents(now, flights, arrivingPredicate, holdingPredicate, returningPredicate);
  }

  @Override
  public List<FlightEventDto> getPhalanxFlightEvents(int galaxy, int system, int position) {
    var coordinates = new Coordinates(galaxy, system, position, CoordinatesKind.PLANET);
    var flights = flightViewRepository.findAllByStartCoordinatesOrTargetCoordinates(coordinates,
        coordinates);

    var now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));

    // Tests if the phalanx is used at the starting coordinates of a given flight.
    Predicate<FlightView> isStart = (FlightView flight) -> coordinates.equals(flight.getStartCoordinates());

    // Deployment should not be visible at the starting coordinates.
    Predicate<FlightView> arrivingPredicate = (FlightView flight) ->
        flight.getMission() != Mission.DEPLOYMENT || !isStart.test(flight);

    // A holding event is going to be visible only at a target.
    Predicate<FlightView> holdingPredicate = (FlightView flight) -> !isStart.test(flight);

    // A returning (recalled) deployment should not be visible. Other returning flights should be only visible at
    // starting coordinates.
    Predicate<FlightView> returningPredicate = (FlightView flight) ->
        flight.getMission() != Mission.DEPLOYMENT && isStart.test(flight);

    return createFlightEvents(now, flights, arrivingPredicate, holdingPredicate, returningPredicate);
  }

  private List<FlightEventDto> createFlightEvents(Date now, List<FlightView> flights,
                                                  Predicate<FlightView> arrivingPredicate,
                                                  Predicate<FlightView> holdingPredicate,
                                                  Predicate<FlightView> returningPredicate) {
    var userId = CustomUser.getCurrentUserId();

    var events = new ArrayList<FlightEventDto>();
    for (var flight : flights) {
      var own = flight.getStartUserId() == userId;

      var startCoordinates = Converter.convert(flight.getStartCoordinates());
      var targetCoordinates = Converter.convert(flight.getTargetCoordinates());
      var mission = Converter.convert(flight.getMission());
      var resources = Converter.convert(flight.getResources());
      var units = FlightUtils.convertUnitsWithPositiveCount(flight.getUnits());

      BiFunction<Date, FlightEventKindDto, FlightEventDto> createEventDto = (var at, var kind) ->
          new FlightEventDto(flight.getId(), at, flight.getStartUserId(), flight.getStartBodyId(), startCoordinates,
              flight.getTargetUserId(), flight.getTargetBodyId(), targetCoordinates, flight.getPartyId(), mission,
              resources, units, own, kind);

      var recalled = flight.getArrivalAt() == null;
      var arriving = !recalled && flight.getArrivalAt().after(now);
      var holding = !recalled && mission == MissionDto.HOLD && flight.getHoldUntil().after(now);
      var returning = recalled || (mission != MissionDto.DEPLOYMENT && mission != MissionDto.MISSILE_ATTACK);

      if (arriving && arrivingPredicate.test(flight)) {
        assert flight.getArrivalAt() != null;
        events.add(createEventDto.apply(flight.getArrivalAt(), FlightEventKindDto.ARRIVING));
      }

      if (holding && holdingPredicate.test(flight)) {
        assert flight.getHoldUntil() != null;
        events.add(createEventDto.apply(flight.getHoldUntil(), FlightEventKindDto.HOLDING));
      }

      if (returning && returningPredicate.test(flight)) {
        assert flight.getReturnAt() != null;
        events.add(createEventDto.apply(flight.getReturnAt(), FlightEventKindDto.RETURNING));
      }
    }

    events.sort(Comparator.comparing(FlightEventDto::getAt).thenComparing(FlightEventDto::getId));
    return events;
  }
}
