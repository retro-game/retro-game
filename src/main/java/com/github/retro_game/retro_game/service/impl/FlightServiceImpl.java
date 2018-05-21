package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.*;
import com.github.retro_game.retro_game.model.repository.*;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.dto.*;
import com.github.retro_game.retro_game.service.exception.*;
import com.github.retro_game.retro_game.service.impl.battleengine.BattleEngine;
import com.github.retro_game.retro_game.service.impl.battleengine.BattleOutcome;
import com.github.retro_game.retro_game.service.impl.battleengine.Combatant;
import com.github.retro_game.retro_game.service.impl.battleengine.CombatantOutcome;
import com.github.retro_game.retro_game.service.impl.item.unit.UnitItem;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple4;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
class FlightServiceImpl implements FlightServiceInternal {
  // The battle engine uses only one byte per player, thus we have to limit the max number of combatants per party. This
  // could be max 256.
  private static final int MAX_COMBATANTS = 64;
  private static final Logger logger = LoggerFactory.getLogger(FlightServiceImpl.class);
  private final boolean astrophysicsBasedColonization;
  private final int maxPlanets;
  private final BattleEngine battleEngine;
  private final BodyRepository bodyRepository;
  private final BodyUnitRepository bodyUnitRepository;
  private final DebrisFieldRepository debrisFieldRepository;
  private final FlightRepository flightRepository;
  private final FlightUnitRepository flightUnitRepository;
  private final FlightViewRepository flightViewRepository;
  private final PartyRepository partyRepository;
  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private ActivityService activityService;
  private BodyServiceInternal bodyServiceInternal;
  private EventScheduler eventScheduler;
  private ReportServiceInternal reportServiceInternal;
  private UnitService unitService;

  FlightServiceImpl(@Value("${retro-game.astrophysics-based-colonization}") boolean astrophysicsBasedColonization,
                    @Value("${retro-game.max-planets}") int maxPlanets,
                    BattleEngine battleEngine, BodyRepository bodyRepository, BodyUnitRepository bodyUnitRepository,
                    DebrisFieldRepository debrisFieldRepository, EventRepository eventRepository,
                    FlightRepository flightRepository, FlightUnitRepository flightUnitRepository,
                    FlightViewRepository flightViewRepository, PartyRepository partyRepository,
                    UserRepository userRepository) {
    this.astrophysicsBasedColonization = astrophysicsBasedColonization;
    this.maxPlanets = maxPlanets;
    this.battleEngine = battleEngine;
    this.bodyRepository = bodyRepository;
    this.bodyUnitRepository = bodyUnitRepository;
    this.debrisFieldRepository = debrisFieldRepository;
    this.eventRepository = eventRepository;
    this.flightRepository = flightRepository;
    this.flightUnitRepository = flightUnitRepository;
    this.flightViewRepository = flightViewRepository;
    this.partyRepository = partyRepository;
    this.userRepository = userRepository;
  }

  @Autowired
  public void setActivityService(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Autowired
  void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Autowired
  void setEventScheduler(EventScheduler eventScheduler) {
    this.eventScheduler = eventScheduler;
  }

  @Autowired
  void setReportServiceInternal(ReportServiceInternal reportServiceInternal) {
    this.reportServiceInternal = reportServiceInternal;
  }

  @Autowired
  public void setUnitService(UnitService unitService) {
    this.unitService = unitService;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByStartOrTargetIn(Collection<Body> bodies) {
    return flightRepository.existsByStartBodyInOrTargetBodyIn(bodies, bodies);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OverviewFlightEventDto> getOverviewFlightEvents(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    Set<Long> partiesIds = user.getParties().stream().map(Party::getId).collect(Collectors.toSet());
    List<FlightView> flights = flightViewRepository.findAllByStartUserIdOrTargetUserIdOrPartyIdIn(user.getId(),
        user.getId(), partiesIds);

    List<OverviewFlightEventDto> events = new ArrayList<>();
    for (FlightView flight : flights) {
      boolean own = flight.getStartUserId() == userId;
      boolean arriving = flight.getArrivalAt() != null && flight.getArrivalAt().after(now);
      boolean holding = flight.getArrivalAt() != null && flight.getMission() == Mission.HOLD &&
          flight.getHoldUntil().after(now);

      // Somebody else's returning flights shouldn't be visible.
      if (!own && !arriving && !holding) {
        continue;
      }

      CoordinatesDto startCoordinates = Converter.convert(flight.getStartCoordinates());
      CoordinatesDto targetCoordinates = Converter.convert(flight.getTargetCoordinates());
      MissionDto mission = Converter.convert(flight.getMission());
      ResourcesDto resources = Converter.convert(flight.getResources());
      Map<UnitKindDto, Integer> units = Converter.convertToEnumMap(flight.getUnits(), UnitKindDto.class,
          Converter::convert, FlightUnit::getCount);

      if (arriving) {
        events.add(new OverviewFlightEventDto(flight.getId(), flight.getArrivalAt(), flight.getStartUserId(),
            flight.getStartUserName(), flight.getStartBodyName(), startCoordinates, flight.getTargetUserId(),
            flight.getTargetUserName(), flight.getTargetBodyName(), targetCoordinates, flight.getPartyId(), mission,
            resources, units, own, FlightEventKindDto.ARRIVING));
      }

      if (holding) {
        events.add(new OverviewFlightEventDto(flight.getId(), flight.getHoldUntil(), flight.getStartUserId(),
            flight.getStartUserName(), flight.getStartBodyName(), startCoordinates, flight.getTargetUserId(),
            flight.getTargetUserName(), flight.getTargetBodyName(), targetCoordinates, flight.getPartyId(), mission,
            resources, units, own, FlightEventKindDto.HOLDING));
      }

      if (own && ((mission != MissionDto.DEPLOYMENT && mission != MissionDto.MISSILE_ATTACK)
          || flight.getArrivalAt() == null)) {
        events.add(new OverviewFlightEventDto(flight.getId(), flight.getReturnAt(), flight.getStartUserId(),
            flight.getStartUserName(), flight.getStartBodyName(), startCoordinates, flight.getTargetUserId(),
            flight.getTargetUserName(), flight.getTargetBodyName(), targetCoordinates, flight.getPartyId(), mission,
            resources, units, true, FlightEventKindDto.RETURNING));
      }
    }

    events.sort(Comparator.comparing(OverviewFlightEventDto::getAt).thenComparing(OverviewFlightEventDto::getId));
    return events;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PhalanxFlightEventDto> getPhalanxFlightEvents(int galaxy, int system, int position) {
    long userId = CustomUser.getCurrentUserId();
    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    Coordinates coordinates = new Coordinates(galaxy, system, position, CoordinatesKind.PLANET);
    List<FlightView> flights = flightViewRepository.findAllByStartCoordinatesOrTargetCoordinates(coordinates,
        coordinates);

    List<PhalanxFlightEventDto> events = new ArrayList<>();
    for (FlightView flight : flights) {
      boolean own = flight.getStartUserId() == userId;
      boolean arriving = flight.getArrivalAt() != null && flight.getArrivalAt().after(now);
      boolean holding = flight.getArrivalAt() != null && flight.getMission() == Mission.HOLD &&
          flight.getHoldUntil().after(now);
      boolean isStart = coordinates.equals(flight.getStartCoordinates());

      CoordinatesDto startCoordinates = Converter.convert(flight.getStartCoordinates());
      CoordinatesDto targetCoordinates = Converter.convert(flight.getTargetCoordinates());
      MissionDto mission = Converter.convert(flight.getMission());
      Map<UnitKindDto, Integer> units = Converter.convertToEnumMap(flight.getUnits(), UnitKindDto.class,
          Converter::convert, FlightUnit::getCount);

      if (arriving && (!isStart || flight.getMission() != Mission.DEPLOYMENT)) {
        events.add(new PhalanxFlightEventDto(flight.getId(), flight.getArrivalAt(), flight.getStartUserId(),
            flight.getStartUserName(), flight.getStartBodyName(), startCoordinates, flight.getTargetBodyName(),
            targetCoordinates, flight.getPartyId(), mission, units, own, FlightEventKindDto.ARRIVING));
      }

      if (holding && !isStart) {
        events.add(new PhalanxFlightEventDto(flight.getId(), flight.getHoldUntil(), flight.getStartUserId(),
            flight.getStartUserName(), flight.getStartBodyName(), startCoordinates, flight.getTargetBodyName(),
            targetCoordinates, flight.getPartyId(), mission, units, own, FlightEventKindDto.HOLDING));
      }

      if (isStart && mission != MissionDto.DEPLOYMENT && mission != MissionDto.MISSILE_ATTACK) {
        events.add(new PhalanxFlightEventDto(flight.getId(), flight.getReturnAt(), flight.getStartUserId(),
            flight.getStartUserName(), flight.getStartBodyName(), startCoordinates, flight.getTargetBodyName(),
            targetCoordinates, flight.getPartyId(), mission, units, own, FlightEventKindDto.RETURNING));
      }
    }

    events.sort(Comparator.comparing(PhalanxFlightEventDto::getAt).thenComparing(PhalanxFlightEventDto::getId));
    return events;
  }

  @Override
  @Transactional(readOnly = true)
  public List<FlightDto> getFlights(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    List<FlightView> flights = flightViewRepository.findAllByStartUserId(user.getId());
    List<FlightDto> list = new ArrayList<>();
    for (FlightView flight : flights) {
      boolean recallable = flight.getMission() != Mission.MISSILE_ATTACK && flight.getArrivalAt() != null &&
          (flight.getArrivalAt().after(now) ||
              (flight.getMission() == Mission.HOLD && flight.getHoldUntil().after(now)));
      boolean partyCreatable = recallable && flight.getPartyId() == null &&
          (flight.getMission() == Mission.ATTACK || flight.getMission() == Mission.DESTROY);
      Map<UnitKindDto, Integer> units = Converter.convertToEnumMap(flight.getUnits(), UnitKindDto.class,
          Converter::convert, FlightUnit::getCount);
      list.add(new FlightDto(flight.getId(), flight.getStartBodyName(), Converter.convert(flight.getStartCoordinates()),
          flight.getTargetBodyName(), Converter.convert(flight.getTargetCoordinates()), flight.getPartyId(),
          Converter.convert(flight.getMission()), flight.getDepartureAt(), flight.getArrivalAt(), flight.getReturnAt(),
          Converter.convert(flight.getResources()), units, recallable, partyCreatable));
    }
    return list;
  }

  @Override
  @Transactional(readOnly = true)
  public int getOccupiedFlightSlots(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return getOccupiedFlightSlots(user);
  }

  private int getOccupiedFlightSlots(User user) {
    return flightRepository.countByStartUser(user);
  }

  @Override
  @Transactional(readOnly = true)
  public int getMaxFlightSlots(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return getMaxFlightSlots(user);
  }

  private int getMaxFlightSlots(User user) {
    Technology computerTechnology = user.getTechnologies().get(TechnologyKind.COMPUTER_TECHNOLOGY);
    return (computerTechnology == null ? 0 : computerTechnology.getLevel()) + 1;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<UnitKindDto, FlyableUnitInfoDto> getFlyableUnits(long bodyId) {
    Body body = bodyRepository.getOne(bodyId);
    User user = body.getUser();
    return UnitItem.getFleet().entrySet().stream()
        .filter(e -> e.getKey() != UnitKind.SOLAR_SATELLITE)
        .collect(Collectors.toMap(
            e -> Converter.convert(e.getKey()),
            e -> {
              UnitKind kind = e.getKey();
              int count = body.getNumUnits(kind);
              UnitItem item = e.getValue();
              int capacity = item.getCapacity();
              int consumption = item.getConsumption(user);
              int speed = unitService.getSpeed(kind, user);
              double weapons = unitService.getWeapons(kind, user);
              double shield = unitService.getShield(kind, user);
              double armor = unitService.getArmor(kind, user);
              return new FlyableUnitInfoDto(count, capacity, consumption, speed, weapons, shield, armor);
            },
            (a, b) -> {
              throw new IllegalStateException();
            },
            () -> new EnumMap<>(UnitKindDto.class)
        ));
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void send(SendFleetParamsDto params) {
    Body body = bodyServiceInternal.getUpdated(params.getBodyId());
    User user = body.getUser();
    long userId = user.getId();

    if (params.getMission() == MissionDto.MISSILE_ATTACK) {
      logger.warn("Sending fleet failed, tried to send missile attack: userId={}", userId);
      throw new WrongMissionException();
    }

    if (getOccupiedFlightSlots(user) >= getMaxFlightSlots(user)) {
      logger.info("Sending fleet failed, no more free flight slots: userId={} bodyId={}", userId, body.getId());
      throw new NoMoreFreeSlotsException();
    }

    Coordinates coordinates;
    Mission mission;
    Party party = null;
    long partyId = 0;
    if (params.getPartyId() != null) {
      partyId = params.getPartyId();

      if (params.getMission() != MissionDto.ATTACK && params.getMission() != MissionDto.DESTROY) {
        logger.warn("Sending fleet failed, joining party with non-attack/destroy mission: userId={} bodyId={}" +
                " partyId={}",
            userId, body.getId(), partyId);
        throw new WrongMissionException();
      }

      Optional<Party> partyOptional = partyRepository.findById(params.getPartyId());
      if (!partyOptional.isPresent()) {
        logger.warn("Sending fleet failed, party doesn't exist: userId={} bodyId={} partyId={}",
            userId, body.getId(), partyId);
        throw new PartyDoesntExistException();
      }
      party = partyOptional.get();

      if (!party.getUsers().contains(user)) {
        logger.warn("Sending fleet failed, unauthorized party access: userId={} bodyId={} partyId={}",
            userId, body.getId(), partyId);
        throw new UnauthorizedPartyAccessException();
      }

      coordinates = party.getTargetCoordinates();
      mission = party.getMission();
    } else {
      coordinates = Converter.convert(params.getCoordinates());
      mission = Converter.convert(params.getMission());
    }

    if (coordinates.equals(body.getCoordinates())) {
      logger.info("Sending fleet failed, selected start body as target: userId={} bodyId={}", userId, body.getId());
      throw new WrongTargetException();
    }

    Map<UnitKind, Integer> units = new EnumMap<>(UnitKind.class);
    for (Map.Entry<UnitKindDto, Integer> entry : params.getUnits().entrySet()) {
      UnitKind kind = Converter.convert(entry.getKey());
      if (kind == UnitKind.SOLAR_SATELLITE || !UnitItem.getFleet().containsKey(kind)) {
        continue;
      }
      Integer count = entry.getValue();
      if (count != null && count > 0) {
        units.put(kind, count);
      }
    }

    if (units.isEmpty()) {
      logger.info("Sending fleet failed, no units: userId={} bodyId={}", userId, body.getId());
      throw new NoUnitSelectedException();
    }

    // Mission hold requires a hold time to be specified.
    if (mission == Mission.HOLD && params.getHoldTime() == null) {
      logger.warn("Sending fleet failed, no hold time specified: userId={} bodyId={}", userId, body.getId());
      throw new HoldTimeNotSpecifiedException();
    }

    // Some missions require specific ships.
    // Note that a check whether there is a death star when sending fleet on a destroy mission shouldn't be done here,
    // as death stars may be sent later using ACS. Thus, it is better to check it when handling the mission.
    switch (mission) {
      case COLONIZATION: {
        if (!units.containsKey(UnitKind.COLONY_SHIP)) {
          logger.info("Sending fleet failed, colonization without colony ship: userId={} bodyId={}",
              userId, body.getId());
          throw new NoColonyShipSelectedException();
        }
        break;
      }
      case ESPIONAGE: {
        if (!units.containsKey(UnitKind.ESPIONAGE_PROBE)) {
          logger.info("Sending fleet failed, espionage without probe: userId={} bodyId={}", userId, body.getId());
          throw new NoEspionageProbeSelectedException();
        }
        break;
      }
      case HARVEST: {
        if (!units.containsKey(UnitKind.RECYCLER)) {
          logger.info("Sending fleet failed, harvest without recycler: userId={} bodyId={}", userId, body.getId());
          throw new NoRecyclerSelectedException();
        }
        break;
      }
    }

    // Some missions require specific target kind.
    switch (mission) {
      case COLONIZATION: {
        if (coordinates.getKind() != CoordinatesKind.PLANET) {
          logger.info("Sending fleet failed, wrong target kind for colonization: userId={} bodyId={} targetKind={}",
              userId, body.getId(), coordinates.getKind());
          throw new WrongTargetKindException();
        }
        break;
      }
      case DESTROY: {
        if (coordinates.getKind() != CoordinatesKind.MOON) {
          logger.info("Sending fleet failed, wrong target kind for destroy: userId={} bodyId={} targetKind={}",
              userId, body.getId(), coordinates.getKind());
          throw new WrongTargetKindException();
        }
        break;
      }
      case HARVEST: {
        if (coordinates.getKind() != CoordinatesKind.DEBRIS_FIELD) {
          logger.info("Sending fleet failed, wrong target kind for harvest: userId={} bodyId={} targetKind={}",
              userId, body.getId(), coordinates.getKind());
          throw new WrongTargetKindException();
        }
        break;
      }
      default: {
        if (coordinates.getKind() != CoordinatesKind.PLANET && coordinates.getKind() != CoordinatesKind.MOON) {
          logger.info("Sending fleet failed, wrong target kind: userId={} bodyId={} mission={} targetKind={}",
              userId, body.getId(), mission, coordinates.getKind());
          throw new WrongTargetKindException();
        }
        break;
      }
    }

    // Some missions require an existing target.
    Optional<Body> targetBodyOptional;
    switch (mission) {
      case COLONIZATION: {
        targetBodyOptional = Optional.empty();
        break;
      }
      case HARVEST: {
        if (!debrisFieldRepository.existsByKey_GalaxyAndKey_SystemAndKey_Position(coordinates.getGalaxy(),
            coordinates.getSystem(), coordinates.getPosition())) {
          logger.info("Sending fleet failed, harvest with non-existing debris field: userId={} bodyId={}" +
                  " targetCoordinates={}",
              userId, body.getId(), coordinates);
          throw new DebrisFieldDoesntExistException();
        }
        targetBodyOptional = Optional.empty();
        break;
      }
      default: {
        targetBodyOptional = bodyRepository.findByCoordinates(coordinates);
        if (!targetBodyOptional.isPresent()) {
          logger.info("Sending fleet failed, target body doesn't exist: userId={} bodyId={} targetCoordinates={}" +
                  " mission={}",
              userId, body.getId(), coordinates, mission);
          throw new BodyDoesntExistException();
        }
        break;
      }
    }

    // Check target user.
    switch (mission) {
      case ATTACK:
      case DESTROY:
      case ESPIONAGE:
      case HOLD: {
        long targetUserId = targetBodyOptional.get().getUser().getId();
        if (targetUserId == userId) {
          logger.info("Sending fleet failed, wrong target user: userId={} bodyId={} targetUserId={} targetBodyId={}" +
                  " mission={}",
              userId, body.getId(), targetUserId, targetBodyOptional.get().getId(), mission);
          throw new WrongTargetUserException();
        }
        break;
      }
      case DEPLOYMENT: {
        long targetUserId = targetBodyOptional.get().getUser().getId();
        if (targetUserId != userId) {
          logger.info("Sending fleet failed, wrong target user for deployment: userId={} bodyId={} targetUserId={}" +
                  " targetBodyId={}",
              userId, body.getId(), targetUserId, targetBodyOptional.get().getId());
          throw new WrongTargetUserException();
        }
        break;
      }
    }

    // FIXME: calc consumption when the mission is HOLD

    int maxSpeed = calculateMaxSpeed(user, units);
    int distance = calculateDistance(body.getCoordinates(), coordinates);
    int duration = calculateDuration(distance, params.getFactor(), maxSpeed);
    double consumption = calculateConsumption(user, distance, params.getFactor(), maxSpeed, units);
    long capacity = calculateCapacity(units);

    Resources bodyResources = body.getResources();
    if (bodyResources.getDeuterium() < consumption) {
      logger.info("Sending fleet failed, not enough deuterium: userId={} bodyId={}", userId, body.getId());
      throw new NotEnoughDeuteriumException();
    }
    bodyResources.setDeuterium(bodyResources.getDeuterium() - consumption);

    capacity -= consumption;
    if (capacity < 0) {
      logger.info("Sending fleet failed, not enough capacity: userId={} bodyId={}", userId, body.getId());
      throw new NotEnoughCapacityException();
    }

    double metal = Math.floor(Math.min(Math.min(params.getResources().getMetal(), capacity), bodyResources.getMetal()));
    capacity -= metal;
    double crystal = Math.floor(Math.min(Math.min(params.getResources().getCrystal(), capacity),
        bodyResources.getCrystal()));
    capacity -= crystal;
    double deuterium = Math.floor(Math.min(Math.min(params.getResources().getDeuterium(), capacity),
        bodyResources.getDeuterium()));
    Resources flightResources = new Resources(metal, crystal, deuterium);
    bodyResources.sub(flightResources);

    // Create a flight.

    Flight flight = new Flight();
    flight.setStartUser(user);
    flight.setStartBody(body);

    long targetUserId = 0;
    long targetBodyId = 0;
    if (targetBodyOptional.isPresent()) {
      Body targetBody = targetBodyOptional.get();
      targetBodyId = targetBody.getId();
      User targetUser = targetBody.getUser();
      targetUserId = targetUser.getId();
      flight.setTargetUser(targetUser);
      flight.setTargetBody(targetBody);
    }
    flight.setTargetCoordinates(coordinates);

    long now = body.getUpdatedAt().toInstant().getEpochSecond();
    flight.setDepartureAt(Date.from(Instant.ofEpochSecond(now)));
    if (party == null) {
      long arrivalAt = now + duration;
      flight.setArrivalAt(Date.from(Instant.ofEpochSecond(arrivalAt)));

      if (mission == Mission.HOLD) {
        long holdUntil = arrivalAt + 3600 * params.getHoldTime();
        flight.setHoldUntil(Date.from(Instant.ofEpochSecond(holdUntil)));
        flight.setReturnAt(Date.from(Instant.ofEpochSecond(holdUntil + duration)));
      } else {
        flight.setReturnAt(Date.from(Instant.ofEpochSecond(arrivalAt + duration)));
      }
    } else {
      List<Flight> flights = flightRepository.findByPartyOrderById(party);
      if (flights.isEmpty()) {
        logger.error("Sending fleet failed, dangling party: userId={} bodyId={} partyId={}",
            userId, body.getId(), party.getId());
        throw new PartyDoesntExistException();
      }

      if (flights.size() >= MAX_COMBATANTS) {
        logger.info("Sending fleet failed, too many party flights: userId={} bodyId={} partyId={}",
            userId, body.getId(), party.getId());
        throw new TooManyPartyFlightsException();
      }

      long newArrivalAt = now + duration;

      long currentArrivalAt = flights.get(0).getArrivalAt().toInstant().getEpochSecond();
      long remaining = currentArrivalAt - now;
      long maxArrivalAt = currentArrivalAt + (long) (0.3 * remaining);

      // Note that, even when the remaining time is negative (when the scheduler is lagging), the check bellow would
      // work fine.

      if (newArrivalAt > maxArrivalAt) {
        logger.info("Sending fleet failed, too late to join the party: userId={} bodyId={} partyId={}",
            userId, body.getId(), party.getId());
        throw new TooLateException();
      }

      if (currentArrivalAt >= newArrivalAt) {
        flight.setArrivalAt(Date.from(Instant.ofEpochSecond(currentArrivalAt)));
        flight.setReturnAt(Date.from(Instant.ofEpochSecond(currentArrivalAt + duration)));
      } else {
        Date arrivalAt = Date.from(Instant.ofEpochSecond(newArrivalAt));

        flight.setArrivalAt(arrivalAt);
        flight.setReturnAt(Date.from(Instant.ofEpochSecond(newArrivalAt + duration)));

        long firstId = Long.MAX_VALUE;
        long diff = newArrivalAt - currentArrivalAt;
        for (Flight f : flights) {
          firstId = Math.min(firstId, f.getId());
          f.setArrivalAt(arrivalAt);
          long returnAt = f.getReturnAt().toInstant().getEpochSecond() + diff;
          f.setReturnAt(Date.from(Instant.ofEpochSecond(returnAt)));
        }
        assert firstId != Long.MAX_VALUE;

        Optional<Event> eventOptional = eventRepository.findFirstByKindAndParam(EventKind.FLIGHT, firstId);
        if (!eventOptional.isPresent()) {
          // Shouldn't happen.
          logger.error("Sending fleet failed, event for the party doesn't exist: userId={} bodyId={} partyId={}",
              userId, body.getId(), party.getId());
          throw new FlightDoesntExistException();
        }

        Event event = eventOptional.get();
        event.setAt(arrivalAt);
        eventScheduler.schedule(event);
      }
    }

    flight.setParty(party);
    flight.setMission(mission);
    flight.setResources(flightResources);
    flightRepository.save(flight);

    // Units.
    Map<UnitKind, BodyUnit> bodyUnits = body.getUnits();
    ArrayList<FlightUnit> flightUnits = new ArrayList<>();
    for (Map.Entry<UnitKind, Integer> entry : units.entrySet()) {
      UnitKind kind = entry.getKey();
      int count = entry.getValue();
      BodyUnit bodyUnit = bodyUnits.get(kind);
      if (bodyUnit == null || bodyUnit.getCount() < count) {
        logger.info("Sending fleet failed, not enough units: userId={} bodyId={}", userId, body.getId());
        throw new NotEnoughUnitsException();
      }
      if (bodyUnit.getCount() == count) {
        bodyUnitRepository.delete(bodyUnit);
      } else {
        bodyUnit.setCount(bodyUnit.getCount() - count);
      }
      FlightUnitKey key = new FlightUnitKey();
      key.setFlight(flight);
      key.setKind(kind);
      FlightUnit flightUnit = new FlightUnit();
      flightUnit.setKey(key);
      flightUnit.setCount(count);
      flightUnits.add(flightUnit);
    }
    flightUnitRepository.saveAll(flightUnits);

    if (party == null) {
      // Add event.
      Event event = new Event();
      event.setAt(flight.getArrivalAt());
      event.setKind(EventKind.FLIGHT);
      event.setParam(flight.getId());
      eventScheduler.schedule(event);
    }

    if (logger.isInfoEnabled()) {
      String unitsString = units.entrySet().stream()
          .map(entry -> entry.getValue() + " " + entry.getKey())
          .collect(Collectors.joining(", "));
      logger.info("Sending fleet: userId={} bodyId={} flightId={} targetUserId={} targetBodyId={}" +
              " targetCoordinates={} partyId={} departureAt='{}' arrivalAt='{}' returnAt='{}' holdUntil={} mission={}" +
              " resources={} units='{}'",
          userId, body.getId(), flight.getId(), targetUserId, targetBodyId, coordinates, partyId,
          flight.getDepartureAt(), flight.getArrivalAt(), flight.getReturnAt(), flight.getHoldUntil(), mission,
          flightResources, unitsString);
    }
  }

  private int calculateMaxSpeed(User user, Map<UnitKind, Integer> units) {
    assert !units.isEmpty();
    OptionalInt maxOptional = units.keySet().stream()
        .mapToInt(kind -> unitService.getSpeed(kind, user))
        .min();
    assert maxOptional.isPresent();
    return maxOptional.getAsInt();
  }

  private int calculateDistance(Coordinates a, Coordinates b) {
    if (a.getGalaxy() != b.getGalaxy()) {
      int diff = Math.abs(a.getGalaxy() - b.getGalaxy());
      return 20000 * Math.min(diff, 5 - diff);
    }
    if (a.getSystem() != b.getSystem()) {
      int diff = Math.abs(a.getSystem() - b.getSystem());
      return 95 * Math.min(diff, 500 - diff) + 2700;
    }
    if (a.getPosition() != b.getPosition()) {
      int diff = Math.abs(a.getPosition() - b.getPosition());
      return 5 * diff + 1000;
    }
    return 5;
  }

  private int calculateDuration(int distance, int factor, int maxSpeed) {
    assert factor >= 1 && factor <= 10;
    assert maxSpeed > 0;
    return (int) Math.round(35000.0 / factor * Math.sqrt(10.0 * distance / maxSpeed)) + 10;
  }

  private double calculateConsumption(User user, int distance, int factor, int maxSpeed, Map<UnitKind, Integer> units) {
    assert factor >= 1 && factor <= 10;
    assert !units.isEmpty();
    Map<UnitKind, UnitItem> fleet = UnitItem.getFleet();
    double f = 0.1 * factor;
    return 1 + Math.round(units.entrySet().stream()
        .mapToDouble(e -> {
          UnitKind kind = e.getKey();
          UnitItem unit = fleet.get(kind);
          int count = e.getValue();
          double x = f * Math.sqrt((double) maxSpeed / unitService.getSpeed(kind, user)) + 1.0;
          return count * ((double) unit.getConsumption(user) * distance / 35000.0) * x * x;
        })
        .sum());
  }

  private long calculateCapacity(Map<UnitKind, Integer> units) {
    assert !units.isEmpty();
    Map<UnitKind, UnitItem> fleet = UnitItem.getFleet();
    return units.entrySet().stream()
        .mapToLong(e -> (long) e.getValue() * fleet.get(e.getKey()).getCapacity())
        .sum();
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void sendProbes(long bodyId, CoordinatesDto targetCoordinates, int numProbes) {
    Map<UnitKindDto, Integer> units = Collections.singletonMap(UnitKindDto.ESPIONAGE_PROBE, numProbes);
    int holdTime = 0;
    int factor = 10;
    ResourcesDto resources = new ResourcesDto(0.0, 0.0, 0.0);
    SendFleetParamsDto params = new SendFleetParamsDto(bodyId, units, MissionDto.ESPIONAGE, holdTime, targetCoordinates,
        factor, resources, null);
    send(params);
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void sendMissiles(long bodyId, CoordinatesDto targetCoordinates, int numMissiles) {
    Body body = bodyServiceInternal.getUpdated(bodyId);
    User user = body.getUser();
    long userId = user.getId();

    int diff = Math.abs(body.getCoordinates().getSystem() - targetCoordinates.getSystem());
    diff = Math.min(diff, 500 - diff);
    int impulseLevel = user.getTechnologyLevel(TechnologyKind.IMPULSE_DRIVE);
    int reach = 5 * impulseLevel - 1;
    if (body.getCoordinates().getGalaxy() != targetCoordinates.getGalaxy() || diff > reach) {
      logger.info("Sending missiles failed, target out of range: userId={} bodyId={} targetCoordinates={}" +
              " numMissiles={}",
          userId, bodyId, targetCoordinates, numMissiles);
      throw new TargetOutOfRangeException();
    }

    Coordinates coords = Converter.convert(targetCoordinates);
    Optional<Body> optionalTargetBody = bodyRepository.findByCoordinates(coords);
    if (!optionalTargetBody.isPresent()) {
      logger.info("Sending missiles failed, target doesn't exist: userId={} bodyId={} targetCoordinates={}" +
              " numMissiles={}",
          userId, bodyId, targetCoordinates, numMissiles);
      throw new BodyDoesntExistException();
    }
    Body targetBody = optionalTargetBody.get();

    User targetUser = targetBody.getUser();
    if (targetUser.getId() == userId) {
      logger.info("Sending missiles failed, wrong target user: userId={} bodyId={} targetCoordinates={}" +
              " numMissiles={}",
          userId, bodyId, targetCoordinates, numMissiles);
      throw new WrongTargetUserException();
    }

    BodyUnit ipm = body.getUnits().get(UnitKind.INTERPLANETARY_MISSILE);
    if (ipm == null || ipm.getCount() < numMissiles) {
      logger.info("Sending missiles failed, not enough missiles: userId={} bodyId={} targetCoordinates={}" +
              " numMissiles={}",
          userId, bodyId, targetCoordinates, numMissiles);
      throw new NotEnoughUnitsException();
    }
    int count = ipm.getCount() - numMissiles;
    if (count == 0) {
      bodyUnitRepository.delete(ipm);
    } else {
      ipm.setCount(count);
    }

    long now = body.getUpdatedAt().toInstant().getEpochSecond();
    long duration = 30 + 60 * diff;
    long arrivalAt = now + duration;
    // Return time doesn't matter in missile attacks, but the column is not nullable and it must differ from arrival
    // time, because handle() function compares these times and decides whether it should handle an attack or a return.
    long returnAt = arrivalAt + duration;

    Flight f = new Flight();
    f.setStartUser(user);
    f.setStartBody(body);
    f.setTargetUser(targetUser);
    f.setTargetBody(targetBody);
    f.setTargetCoordinates(coords);
    f.setDepartureAt(Date.from(Instant.ofEpochSecond(now)));
    f.setArrivalAt(Date.from(Instant.ofEpochSecond(arrivalAt)));
    f.setReturnAt(Date.from(Instant.ofEpochSecond(returnAt)));
    f.setMission(Mission.MISSILE_ATTACK);
    f.setResources(new Resources());
    flightRepository.save(f);

    FlightUnitKey key = new FlightUnitKey();
    key.setFlight(f);
    key.setKind(UnitKind.INTERPLANETARY_MISSILE);
    FlightUnit fu = new FlightUnit();
    fu.setKey(key);
    fu.setCount(numMissiles);
    flightUnitRepository.save(fu);

    // Add event.
    Event event = new Event();
    event.setAt(f.getArrivalAt());
    event.setKind(EventKind.FLIGHT);
    event.setParam(f.getId());
    eventScheduler.schedule(event);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recall(long bodyId, long flightId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<Flight> flightOptional = flightRepository.findById(flightId);
    if (!flightOptional.isPresent()) {
      logger.warn("Recalling flight failed, flight doesn't exist: userId={} flightId={}", userId, flightId);
      throw new FlightDoesntExistException();
    }
    Flight flight = flightOptional.get();

    if (flight.getStartUser().getId() != userId) {
      logger.warn("Recalling flight failed, unauthorized access: userId={} flightId={}", userId, flightId);
      throw new UnauthorizedFlightAccessException();
    }

    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    boolean recallable = flight.getMission() != Mission.MISSILE_ATTACK && flight.getArrivalAt() != null &&
        (flight.getArrivalAt().after(now) || (flight.getMission() == Mission.HOLD && flight.getHoldUntil().after(now)));
    if (!recallable) {
      logger.warn("Recalling flight failed, flight is unrecallable: userId={} flightId={}", userId, flightId);
      throw new UnrecallableFlightException();
    }

    // If the mission is hold, we can recall before or after arrival.
    long departureAt = flight.getDepartureAt().toInstant().getEpochSecond();
    if (flight.getMission() == Mission.HOLD && !flight.getArrivalAt().after(now)) {
      assert flight.getArrivalAt() != null;
      long arrivalAt = flight.getArrivalAt().toInstant().getEpochSecond();
      long returnAt = now.toInstant().getEpochSecond() + (arrivalAt - departureAt);
      flight.setHoldUntil(now);
      flight.setReturnAt(Date.from(Instant.ofEpochSecond(returnAt)));
    } else {
      long returnAt = departureAt + 2 * (now.toInstant().getEpochSecond() - departureAt);
      flight.setArrivalAt(null);
      flight.setReturnAt(Date.from(Instant.ofEpochSecond(returnAt)));
    }

    Optional<Event> eventOptional = eventRepository.findFirstByKindAndParam(EventKind.FLIGHT, flight.getId());

    scheduleReturn(flight);

    if (flight.getParty() == null) {
      if (!eventOptional.isPresent()) {
        // This shouldn't happen.
        logger.error("Recalling flight, flight exists without event: userId={} flightId={}", userId, flightId);
      } else {
        logger.info("Recalling flight: userId={} flightId={}", userId, flightId);
        eventRepository.delete(eventOptional.get());
      }
      return;
    }

    Party party = flight.getParty();

    // The fleet must exit the party.
    flight.setParty(null);
    flightRepository.save(flight);

    if (!eventOptional.isPresent()) {
      logger.info("Recalling flight, the fleet is in a party, but it is not the leading one: userId={} flightId={}" +
              " partyId={}",
          userId, flightId, party.getId());
      return;
    }
    Event event = eventOptional.get();

    List<Flight> flights = flightRepository.findByPartyOrderById(party);

    if (flights.isEmpty()) {
      logger.info("Recalling flight, deleting party: userId={} flightId={} partyId={}",
          userId, flightId, party.getId());
      partyRepository.delete(party);
      eventRepository.delete(event);
      return;
    }

    logger.info("Recalling flight, updating the leader of the party: userId={} flightId={} partyId={}",
        userId, flightId, party.getId());
    event.setParam(flights.get(0).getId());
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void handle(Event event) {
    Flight flight = flightRepository.getOne(event.getParam());
    eventRepository.delete(event);
    if (event.getAt().toInstant().getEpochSecond() == flight.getReturnAt().toInstant().getEpochSecond()) {
      handleReturn(flight);
      return;
    }
    switch (flight.getMission()) {
      case ATTACK:
        handleAttack(flight);
        break;
      case COLONIZATION:
        handleColonization(flight);
        break;
      case DEPLOYMENT:
        handleDeployment(flight);
        break;
      case DESTROY:
        handleDestroy(flight);
        break;
      case ESPIONAGE:
        handleEspionage(flight);
        break;
      case HARVEST:
        handleHarvest(flight);
        break;
      case HOLD:
        // Hold mission is handled twice, pass time to know which case it is.
        handleHold(flight, event.getAt());
        break;
      case TRANSPORT:
        handleTransport(flight);
        break;
      case MISSILE_ATTACK:
        handleMissileAttack(flight);
        break;
      default:
        // This shouldn't really happen.
        logger.error("Handling flight event failed, mission not implemented: mission={}", flight.getMission());
        scheduleReturn(flight);
    }
  }

  private void handleReturn(Flight flight) {
    Body body = flight.getStartBody();

    if (logger.isInfoEnabled()) {
      String unitsString = flight.getUnits().entrySet().stream()
          .map(entry -> entry.getValue().getCount() + " " + entry.getKey())
          .collect(Collectors.joining(", "));
      logger.info("Fleet return: flightId={} startUserId={} startBodyId={} targetCoordinates={} departureAt='{}'" +
              " arrivalAt='{}' returnAt='{}' holdUntil='{}' mission={} resources={} units='{}'",
          flight.getId(), flight.getStartUser().getId(), body.getId(), flight.getTargetCoordinates(),
          flight.getDepartureAt(), flight.getArrivalAt(), flight.getReturnAt(), flight.getHoldUntil(),
          flight.getMission(), flight.getResources(), unitsString);
    }

    // Create activity.
    activityService.handleBodyActivity(body.getId(), flight.getReturnAt().toInstant().getEpochSecond());

    bodyServiceInternal.updateResources(body, flight.getReturnAt());
    body.getResources().add(flight.getResources());

    deployUnits(flight, body);
    flightRepository.delete(flight);

    if (flight.getMission() != Mission.ESPIONAGE) {
      reportServiceInternal.createReturnReport(flight);
    }
  }

  private void handleAttack(Flight flight) {
    handleAttackOrDestroy(flight, false);
  }

  private void handleDestroy(Flight flight) {
    handleAttackOrDestroy(flight, true);
  }

  private void handleAttackOrDestroy(Flight flight, boolean destroy) {
    Random random = ThreadLocalRandom.current();

    Body body = flight.getTargetBody();
    Coordinates coordinates = new Coordinates(flight.getTargetCoordinates());
    Date arrivalAt = flight.getArrivalAt();

    // Create activity.
    activityService.handleBodyActivity(body.getId(), arrivalAt.toInstant().getEpochSecond());

    bodyServiceInternal.updateResources(body, arrivalAt);

    List<Flight> attackersFlights;
    long partyId = 0;
    Party party = flight.getParty();
    if (party == null) {
      attackersFlights = new ArrayList<>(Collections.singletonList(flight));
    } else {
      partyId = party.getId();
      attackersFlights = flightRepository.findByPartyOrderById(flight.getParty());

      // We don't need the party anymore.
      for (Flight f : attackersFlights) {
        f.setParty(null);
      }
      partyRepository.delete(party);
    }

    List<Flight> defendersFlights = getHoldingFlights(body, flight.getArrivalAt());

    Combatant[] attackers = null;
    Combatant[] defenders = null;
    BattleOutcome battleOutcome = null;
    BattleResult result = BattleResult.ATTACKERS_WIN;
    Resources attackersLoss = new Resources();
    Resources defendersLoss = new Resources();
    long debrisMetal = 0;
    long debrisCrystal = 0;
    double moonChance = 0.0;
    boolean moonGiven = false;
    int seed = 0;
    long executionTime = 0;
    int numRounds = 0;

    // When there is no units on the target body, we must skip the fight, as the battle engine cannot handle combatants
    // without units.
    boolean fight = !body.getUnits().isEmpty() || !defendersFlights.isEmpty();
    if (fight) {
      // Prepare input for the battle engine.

      attackers = new Combatant[attackersFlights.size()];
      for (int i = 0; i < attackers.length; i++) {
        Flight f = attackersFlights.get(i);
        User u = f.getStartUser();
        Map<UnitKind, Integer> groups = Converter.convertToEnumMap(f.getUnits(), UnitKind.class, Function.identity(),
            FlightUnit::getCount);
        attackers[i] = new Combatant((int) u.getId(), // FIXME: cast
            u.getTechnologyLevel(TechnologyKind.WEAPONS_TECHNOLOGY),
            u.getTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY),
            u.getTechnologyLevel(TechnologyKind.ARMOR_TECHNOLOGY),
            groups);
      }

      defenders = new Combatant[defendersFlights.size() + (body.getUnits().isEmpty() ? 0 : 1)];
      for (int i = 0; i < defendersFlights.size(); i++) {
        Flight f = defendersFlights.get(i);
        User u = f.getStartUser();
        Map<UnitKind, Integer> groups = Converter.convertToEnumMap(f.getUnits(), UnitKind.class, Function.identity(),
            FlightUnit::getCount);
        defenders[i] = new Combatant((int) u.getId(), // FIXME: cast
            u.getTechnologyLevel(TechnologyKind.WEAPONS_TECHNOLOGY),
            u.getTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY),
            u.getTechnologyLevel(TechnologyKind.ARMOR_TECHNOLOGY),
            groups);
      }
      if (!body.getUnits().isEmpty()) {
        User u = flight.getTargetUser();
        Map<UnitKind, Integer> groups = body.getUnits().entrySet().stream()
            .filter(e -> e.getKey() != UnitKind.ANTI_BALLISTIC_MISSILE && e.getKey() != UnitKind.INTERPLANETARY_MISSILE)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getCount(),
                (a, b) -> {
                  throw new IllegalStateException();
                },
                () -> new EnumMap<>(UnitKind.class)));
        defenders[defenders.length - 1] = new Combatant((int) u.getId(), // FIXME: cast
            u.getTechnologyLevel(TechnologyKind.WEAPONS_TECHNOLOGY),
            u.getTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY),
            u.getTechnologyLevel(TechnologyKind.ARMOR_TECHNOLOGY),
            groups);
      }

      // Fight!
      seed = random.nextInt();
      long startTime = System.nanoTime();
      battleOutcome = battleEngine.fight(attackers, defenders, seed);
      executionTime = System.nanoTime() - startTime;

      // Process the result.

      numRounds = battleOutcome.getNumRounds();
      Map<UnitKind, UnitItem> items = UnitItem.getAll();

      // Helper for flights.
      final int nRounds = numRounds; // fucking java
      Function<Tuple2<List<Flight>, CombatantOutcome[]>, Tuple4<Integer, Resources, Long, Long>> handleFlights =
          (pair) -> {
            List<Flight> flights = pair._1;
            CombatantOutcome[] outcomes = pair._2;
            int totalRemaining = 0;
            Resources loss = new Resources();
            long metal = 0;
            long crystal = 0;
            for (int i = 0; i < flights.size(); i++) {
              Flight f = flights.get(i);
              CombatantOutcome outcome = outcomes[i];
              for (Iterator<Map.Entry<UnitKind, FlightUnit>> it = f.getUnits().entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<UnitKind, FlightUnit> entry = it.next();

                UnitKind kind = entry.getKey();
                FlightUnit unit = entry.getValue();

                int remaining = (int) outcome.getNumRemainingUnits(nRounds - 1, kind.ordinal());
                totalRemaining += remaining;
                int diff = unit.getCount() - remaining;
                assert diff >= 0;

                if (remaining == 0) {
                  flightUnitRepository.delete(unit);
                  it.remove();
                } else {
                  unit.setCount(remaining);
                }

                Resources cost = new Resources(items.get(kind).getCost());
                cost.mul(diff);
                loss.add(cost);

                metal += cost.getMetal();
                crystal += cost.getCrystal();
              }
            }
            return Tuple.of(totalRemaining, loss, metal, crystal);
          };

      // Attackers' flights.
      CombatantOutcome[] attackersOutcomes = battleOutcome.getAttackersOutcomes();
      Tuple4<Integer, Resources, Long, Long> at = handleFlights.apply(Tuple.of(attackersFlights, attackersOutcomes));
      int attackersTotalRemaining = at._1;
      attackersLoss = at._2;
      debrisMetal += at._3;
      debrisCrystal += at._4;

      CombatantOutcome[] defendersOutcomes = battleOutcome.getDefendersOutcomes();

      // Defenders' flights.
      Tuple4<Integer, Resources, Long, Long> dt = handleFlights.apply(Tuple.of(defendersFlights, defendersOutcomes));
      int defendersTotalRemaining = dt._1;
      defendersLoss = dt._2;
      debrisMetal += dt._3;
      debrisCrystal += dt._4;

      // Defender's body.
      if (!body.getUnits().isEmpty()) {
        CombatantOutcome outcome = defendersOutcomes[defendersOutcomes.length - 1];
        for (Iterator<Map.Entry<UnitKind, BodyUnit>> it = body.getUnits().entrySet().iterator(); it.hasNext(); ) {
          Map.Entry<UnitKind, BodyUnit> entry = it.next();

          UnitKind kind = entry.getKey();
          BodyUnit unit = entry.getValue();
          boolean isDefense = UnitItem.getDefense().containsKey(kind);

          int remaining = (int) outcome.getNumRemainingUnits(numRounds - 1, kind.ordinal());
          defendersTotalRemaining += remaining;
          int diff = unit.getCount() - remaining;
          assert diff >= 0;

          Resources cost = new Resources(items.get(kind).getCost());
          cost.mul(diff);
          defendersLoss.add(cost);

          if (!isDefense) {
            debrisMetal += cost.getMetal();
            debrisCrystal += cost.getCrystal();
          }

          if (isDefense) {
            // Rebuild defense.
            remaining += (int) Math.floor(0.7 * diff);
          }

          if (remaining == 0) {
            bodyUnitRepository.delete(unit);
            it.remove();
          } else {
            unit.setCount(remaining);
          }
        }
      }

      if (defendersTotalRemaining > 0) {
        result = attackersTotalRemaining > 0 ? BattleResult.DRAW : BattleResult.DEFENDERS_WIN;
      }

      // Handle debris field and maybe create a moon.
      if (debrisMetal != 0 || debrisCrystal != 0) {
        debrisMetal = (long) Math.floor(0.3 * debrisMetal);
        debrisCrystal = (long) Math.floor(0.3 * debrisCrystal);

        DebrisFieldKey debrisFieldKey = new DebrisFieldKey();
        debrisFieldKey.setGalaxy(coordinates.getGalaxy());
        debrisFieldKey.setSystem(coordinates.getSystem());
        debrisFieldKey.setPosition(coordinates.getPosition());

        DebrisField debrisField = debrisFieldRepository.findById(debrisFieldKey).orElse(null);
        if (debrisField != null) {
          debrisField.setMetal(debrisField.getMetal() + debrisMetal);
          debrisField.setCrystal(debrisField.getCrystal() + debrisCrystal);
        } else {
          debrisField = new DebrisField();
          debrisField.setKey(debrisFieldKey);
          debrisField.setCreatedAt(arrivalAt);
          debrisField.setMetal(debrisMetal);
          debrisField.setCrystal(debrisCrystal);
        }

        debrisField.setUpdatedAt(arrivalAt);
        debrisFieldRepository.save(debrisField);

        moonChance = Math.min(0.2, 1e-7 * (debrisMetal + debrisCrystal));
        if (moonChance >= 0.01 && moonChance > random.nextDouble()) {
          Coordinates moonCoordinates = new Coordinates(coordinates.getGalaxy(), coordinates.getSystem(),
              coordinates.getPosition(), CoordinatesKind.MOON);
          if (!bodyRepository.existsByCoordinates(moonCoordinates)) {
            moonGiven = true;
            Body moon = bodyServiceInternal.createMoon(flight.getTargetUser(), moonCoordinates, arrivalAt, moonChance);
            bodyRepository.save(moon);
          }
        }
      }
    }

    // Check whether the target is a moon too, as it may have been destroyed before this attack and the flights
    // redirected to the corresponding planet.
    if (destroy && result == BattleResult.ATTACKERS_WIN && body.getCoordinates().getKind() == CoordinatesKind.MOON) {
      int totalDSs = attackersFlights.stream()
          .mapToInt(f -> {
            FlightUnit deathStars = f.getUnits().get(UnitKind.DEATH_STAR);
            return deathStars != null ? deathStars.getCount() : 0;
          })
          .sum();
      int diameter = body.getDiameter();

      // Note that even when totalDSs is 0, this algorithm would still work fine.

      double moonDestructionChance = (1.0 - 0.01 * Math.sqrt(diameter)) * Math.sqrt(totalDSs);
      if (moonDestructionChance > random.nextDouble()) {
        // Redirect all flights to the corresponding planet.

        Coordinates c = new Coordinates(body.getCoordinates().getGalaxy(), body.getCoordinates().getSystem(),
            body.getCoordinates().getPosition(), CoordinatesKind.PLANET);
        Optional<Body> optionalPlanet = bodyRepository.findByCoordinates(c);
        Assert.isTrue(optionalPlanet.isPresent(), "A moon without a planet shouldn't exist");
        Body planet = optionalPlanet.get();

        // FIXME: is updating attackers/defenders flights necessary if we update all flights by target body later?

        for (Flight f : attackersFlights) {
          f.setTargetBody(planet);
          f.getTargetCoordinates().setKind(CoordinatesKind.PLANET);
        }

        for (Flight f : defendersFlights) {
          f.setTargetBody(planet);
          f.getTargetCoordinates().setKind(CoordinatesKind.PLANET);
        }

        for (Flight f : flightRepository.findByStartBody(body)) {
          f.setStartBody(planet);
        }

        for (Flight f : flightRepository.findByTargetBody(body)) {
          f.setTargetBody(planet);
          f.getTargetCoordinates().setKind(CoordinatesKind.PLANET);
        }

        bodyServiceInternal.destroyMoon(body);
      }

      double dssDestructionChance = 0.005 * Math.sqrt(diameter);
      if (dssDestructionChance > random.nextDouble()) {
        for (Flight f : attackersFlights) {
          FlightUnit ds = f.getUnits().remove(UnitKind.DEATH_STAR);
          if (ds != null) {
            flightUnitRepository.delete(ds);
          }
        }
      }
    }

    // Save owners of fleets to generate reports later.
    Set<User> attackersUsers = attackersFlights.stream().map(Flight::getStartUser).collect(Collectors.toSet());
    Set<User> defendersUsers = defendersFlights.stream().map(Flight::getStartUser).collect(Collectors.toSet());
    defendersUsers.add(flight.getTargetUser());

    // Delete fleets that have no units left.

    for (Iterator<Flight> it = attackersFlights.iterator(); it.hasNext(); ) {
      Flight f = it.next();
      if (f.getUnits().isEmpty()) {
        flightRepository.delete(f);
        it.remove();
      }
    }

    List<Long> deletedIds = new ArrayList<>();
    for (Iterator<Flight> it = defendersFlights.iterator(); it.hasNext(); ) {
      Flight f = it.next();
      if (f.getUnits().isEmpty()) {
        deletedIds.add(f.getId());
        flightRepository.delete(f);
        it.remove();
      }
    }
    // We need to delete the events as well.
    List<Event> events = eventRepository.findByKindAndParamIn(EventKind.FLIGHT, deletedIds);
    eventRepository.deleteAll(events);

    Resources plunder = new Resources();
    if (result == BattleResult.ATTACKERS_WIN) {
      // The plunder is max 50% of the body's resources. Each fleet can take at most 1/N of that 50%, where N is the
      // the number of fleets. We keep capacities of the fleets ordered, so that if a fleet isn't able to take the
      // possible loot, then the subsequent fleets can take a bit more. Thus, we take as much as possible. The
      // remaining part of the algorithm is like the original one.

      Resources res = body.getResources();
      long remMetal = (long) res.getMetal() / 2;
      long remCrystal = (long) res.getCrystal() / 2;
      long remDeuterium = (long) res.getDeuterium() / 2;

      Map<Long, Resources> plunders = attackersFlights.stream()
          .collect(Collectors.toMap(Flight::getId, f -> new Resources()));

      List<MutablePair<Long, Long>> capacities = attackersFlights.stream()
          .map(f -> {
            Map<UnitKind, Integer> units = Converter.convertToEnumMap(f.getUnits(), UnitKind.class, Function.identity(),
                FlightUnit::getCount);
            double capacity = calculateCapacity(units);
            Resources r = f.getResources();
            capacity -= r.getMetal() + r.getCrystal() + r.getDeuterium();
            return MutablePair.of(f.getId(), (long) capacity);
          })
          .sorted(Comparator.comparing(MutablePair::getRight))
          .collect(Collectors.toCollection(ArrayList::new));

      // 1. Fill max 1/3 of the capacity with metal.
      for (int i = 0; i < capacities.size(); i++) {
        MutablePair<Long, Long> pair = capacities.get(i);
        long taken = Math.min(pair.right / 3, remMetal / (capacities.size() - i));
        pair.right -= taken;
        remMetal -= taken;
        Resources p = plunders.get(pair.left);
        p.setMetal(taken);
      }

      // 2. Fill max 1/2 of the remaining capacity with crystal.
      for (int i = 0; i < capacities.size(); i++) {
        MutablePair<Long, Long> pair = capacities.get(i);
        long taken = Math.min(pair.right / 2, remCrystal / (capacities.size() - i));
        pair.right -= taken;
        remCrystal -= taken;
        Resources p = plunders.get(pair.left);
        p.setCrystal(taken);
      }

      // 3. Fill the rest with deuterium.
      for (int i = 0; i < capacities.size(); i++) {
        MutablePair<Long, Long> pair = capacities.get(i);
        long taken = Math.min(pair.right, remDeuterium / (capacities.size() - i));
        pair.right -= taken;
        remDeuterium -= taken;
        Resources p = plunders.get(pair.left);
        p.setDeuterium(taken);
      }

      // 4. If there is still some place, fill the half of it with metal.
      for (int i = 0; i < capacities.size(); i++) {
        MutablePair<Long, Long> pair = capacities.get(i);
        long taken = Math.min(pair.right / 2, remMetal / (capacities.size() - i));
        pair.right -= taken;
        remMetal -= taken;
        Resources p = plunders.get(pair.left);
        p.setMetal(p.getMetal() + taken);
      }

      // 5. And the second half with crystal.
      for (int i = 0; i < capacities.size(); i++) {
        MutablePair<Long, Long> pair = capacities.get(i);
        long taken = Math.min(pair.right / 2, remCrystal / (capacities.size() - i));
        // Updating capacity here isn't needed anymore.
        remCrystal -= taken;
        Resources p = plunders.get(pair.left);
        p.setCrystal(p.getCrystal() + taken);
      }

      for (Flight f : attackersFlights) {
        Resources p = plunders.get(f.getId());

        // Update fleet.
        Resources r = f.getResources();
        r.setMetal(r.getMetal() + p.getMetal());
        r.setCrystal(r.getCrystal() + p.getCrystal());
        r.setDeuterium(r.getDeuterium() + p.getDeuterium());
        r.floor();

        // Update total plunder.
        plunder.add(p);
      }

      // Update the body's resources.
      plunder.floor();
      body.getResources().sub(plunder);
    }

    // Schedule return for all remaining attackers' fleets.
    for (Flight f : attackersFlights) {
      scheduleReturn(f);
    }

    // Generate reports.

    CombatReport combatReport = null;
    long combatReportId = 0;
    if (fight) {
      combatReport = reportServiceInternal.createCombatReport(arrivalAt, attackers, defenders, battleOutcome, result,
          attackersLoss, defendersLoss, plunder, debrisMetal, debrisCrystal, moonChance, moonGiven, seed,
          executionTime);
      combatReportId = combatReport.getId();
    }

    for (User user : attackersUsers) {
      reportServiceInternal.createSimplifiedCombatReport(user, true, flight.getArrivalAt(), flight.getTargetUser(),
          coordinates, result, numRounds, attackersLoss, defendersLoss, plunder, debrisMetal, debrisCrystal, moonChance,
          moonGiven, combatReport);
    }

    for (User user : defendersUsers) {
      reportServiceInternal.createSimplifiedCombatReport(user, false, flight.getArrivalAt(), flight.getStartUser(),
          coordinates, result, numRounds, attackersLoss, defendersLoss, plunder, debrisMetal, debrisCrystal, moonChance,
          moonGiven, combatReport);
    }

    if (logger.isInfoEnabled()) {
      String flightsIds = attackersFlights.stream()
          .map(f -> Long.toString(f.getId()))
          .collect(Collectors.joining(", "));
      String startUsersIds = attackersUsers.stream()
          .map(u -> Long.toString(u.getId()))
          .collect(Collectors.joining(", "));
      logger.info("Attack: flightsIds='{}' startUsersIds='{}' partyId={} targetUserId={} targetBodyId={}" +
              " combatReportId={} plunder={} debris={}",
          flightsIds, startUsersIds, partyId, flight.getTargetUser().getId(), flight.getTargetBody().getId(),
          combatReportId, plunder, debrisMetal + debrisCrystal);
    }
  }

  private void handleColonization(Flight flight) {
    User user = flight.getStartUser();
    Coordinates coordinates = flight.getTargetCoordinates();

    int max;
    if (astrophysicsBasedColonization) {
      int level = user.getTechnologyLevel(TechnologyKind.ASTROPHYSICS);
      max = 1 + (level + 1) / 2;
    } else {
      max = maxPlanets;
    }

    if (bodyRepository.existsByCoordinates(coordinates) ||
        bodyRepository.countByUserAndCoordinatesKind(user, CoordinatesKind.PLANET) >= max) {
      logger.info("Colonization failed, target planet exists or max number of planets: flightId={} startUserId={}" +
              " startBodyId={} targetCoordinates={} arrivalAt='{}'",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), coordinates,
          flight.getArrivalAt());
      reportServiceInternal.createColonizationReport(flight, null, null);
      scheduleReturn(flight);
      return;
    }

    Resources resources = flight.getResources();
    flight.setResources(new Resources());

    Body colony = bodyServiceInternal.createColony(user, coordinates, flight.getArrivalAt());
    colony.getResources().add(resources);
    bodyRepository.save(colony);

    // Create activity.
    activityService.handleBodyActivity(colony.getId(), flight.getArrivalAt().toInstant().getEpochSecond());

    reportServiceInternal.createColonizationReport(flight, resources, (double) colony.getDiameter());

    FlightUnit colonyShip = flight.getUnits().get(UnitKind.COLONY_SHIP);
    assert colonyShip != null;
    int numColonyShips = colonyShip.getCount();
    assert numColonyShips >= 1;

    if (numColonyShips == 1) {
      assert flight.getUnits().size() >= 1;
      boolean hasOtherUnits = flight.getUnits().size() > 1;

      flightUnitRepository.delete(colonyShip);

      if (!hasOtherUnits) {
        logger.info("Colonization successful, deleting flight: flightId={} startUserId={} startBodyId={}" +
                " targetCoordinates={} arrivalAt='{}' colonyId={}",
            flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetCoordinates(),
            flight.getArrivalAt(), colony.getId());
        flightRepository.delete(flight);
        return;
      }
    } else {
      colonyShip.setCount(numColonyShips - 1);
    }

    logger.info("Colonization successful, scheduling return: flightId={} startUserId={} startBodyId={}" +
            " targetCoordinates={} arrivalAt='{}' colonyId={}",
        flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetCoordinates(),
        flight.getArrivalAt(), colony.getId());
    scheduleReturn(flight);
  }

  private void handleDeployment(Flight flight) {
    Body body = flight.getTargetBody();

    if (logger.isInfoEnabled()) {
      String unitsString = flight.getUnits().entrySet().stream()
          .map(entry -> entry.getValue().getCount() + " " + entry.getKey())
          .collect(Collectors.joining(", "));
      logger.info("Deployment: flightId={} startUserId={} startBodyId={} targetBodyId={} arrivalAt='{}' resources={}" +
              " units='{}'",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), body.getId(),
          flight.getArrivalAt(), flight.getResources(), unitsString);
    }

    // Create activity.
    activityService.handleBodyActivity(body.getId(), flight.getArrivalAt().toInstant().getEpochSecond());

    bodyServiceInternal.updateResources(body, flight.getArrivalAt());
    body.getResources().add(flight.getResources());

    deployUnits(flight, body);
    flightRepository.delete(flight);

    reportServiceInternal.createDeploymentReport(flight);
  }

  private void handleEspionage(Flight flight) {
    Body body = flight.getTargetBody();

    List<Flight> holdingFlights = getHoldingFlights(flight.getTargetBody(), flight.getArrivalAt());

    double counterChance;
    if (flight.getUnits().size() > 1) {
      // If not only probes sent for espionage, always counter.
      counterChance = 1.0;
    } else {
      FlightUnit probes = flight.getUnits().get(UnitKind.ESPIONAGE_PROBE);
      assert probes != null;
      int numProbes = probes.getCount();

      int numTargetShips = body.getUnits().entrySet().stream()
          .filter(e -> UnitItem.getFleet().containsKey(e.getKey()))
          .mapToInt(e -> e.getValue().getCount())
          .sum();
      numTargetShips += holdingFlights.stream()
          .flatMap(f -> f.getUnits().values().stream())
          .mapToInt(FlightUnit::getCount)
          .sum();

      Technology targetTech = body.getUser().getTechnologies().get(TechnologyKind.ESPIONAGE_TECHNOLOGY);
      int targetLevel = targetTech != null ? targetTech.getLevel() : 0;
      Technology ownTech = flight.getStartUser().getTechnologies().get(TechnologyKind.ESPIONAGE_TECHNOLOGY);
      int ownLevel = ownTech != null ? ownTech.getLevel() : 0;
      int techDiff = targetLevel - ownLevel;

      counterChance = Math.min(1.0, 0.0025 * numProbes * numTargetShips * Math.pow(2.0, techDiff));
    }

    logger.info("Espionage: flightId={} startUserId={} startBodyId={} targetUserId={} targetBodyId={} arrivalAt='{}'",
        flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetUser().getId(),
        flight.getTargetBody().getId(), flight.getArrivalAt());

    reportServiceInternal.createEspionageReport(flight, holdingFlights, counterChance);
    reportServiceInternal.createHostileEspionageReport(flight, counterChance);

    // Create activity after the report was generated, as we need the latest activity there.
    activityService.handleBodyActivity(body.getId(), flight.getArrivalAt().toInstant().getEpochSecond());

    if (counterChance >= 0.01 && counterChance > ThreadLocalRandom.current().nextDouble()) {
      handleAttack(flight);
    } else {
      scheduleReturn(flight);
    }
  }

  private void handleHarvest(Flight flight) {
    Coordinates coordinates = flight.getTargetCoordinates();

    DebrisFieldKey debrisFieldKey = new DebrisFieldKey();
    debrisFieldKey.setGalaxy(coordinates.getGalaxy());
    debrisFieldKey.setSystem(coordinates.getSystem());
    debrisFieldKey.setPosition(coordinates.getPosition());
    Optional<DebrisField> debrisFieldOptional = debrisFieldRepository.findById(debrisFieldKey);
    if (!debrisFieldOptional.isPresent()) {
      logger.error("Harvesting failed, debris field doesn't exist: flightId={} startUserId={} startBodyId={}" +
              " targetCoordinates={} arrivalAt='{}'",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), coordinates,
          flight.getArrivalAt());
    } else {
      Resources flightResources = flight.getResources();
      Map<UnitKind, FlightUnit> units = flight.getUnits();

      long totalCapacity = 0;
      for (Map.Entry<UnitKind, FlightUnit> entry : units.entrySet()) {
        totalCapacity += (long) entry.getValue().getCount() * UnitItem.getFleet().get(entry.getKey()).getCapacity();
      }
      totalCapacity -= (long) Math.ceil(flightResources.getMetal() + flightResources.getCrystal() +
          flightResources.getDeuterium());
      int numRecyclers = units.get(UnitKind.RECYCLER).getCount();
      assert numRecyclers >= 1;
      long recCapacity = (long) numRecyclers * UnitItem.getFleet().get(UnitKind.RECYCLER).getCapacity();
      long capacity = Math.min(recCapacity, totalCapacity);

      DebrisField debrisField = debrisFieldOptional.get();
      long debrisMetal = debrisField.getMetal();
      long debrisCrystal = debrisField.getCrystal();

      long harvestedMetal = Math.min(capacity / 2, debrisMetal);
      long harvestedCrystal = Math.min(capacity / 2, debrisCrystal);

      long remainingMetal = debrisMetal - harvestedMetal;
      long remainingCrystal = debrisCrystal - harvestedCrystal;

      long c = capacity - (harvestedMetal + harvestedCrystal);
      assert c >= 0;
      if (c > 0) {
        assert remainingMetal == 0 || remainingCrystal == 0;
        if (remainingMetal > 0) {
          long tmp = Math.min(c, remainingMetal);
          harvestedMetal += tmp;
          remainingMetal -= tmp;
        } else {
          long tmp = Math.min(c, remainingCrystal);
          harvestedCrystal += tmp;
          remainingCrystal -= tmp;
        }
      }

      logger.info("Harvesting successful: flightId={} startUserId={} startBodyId={} targetCoordinates={}" +
              " arrivalAt='{}' numRecyclers={} capacity={} harvestedMetal={} harvestedCrystal={} remainingMetal={}" +
              " remainingCrystal={}",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), coordinates,
          flight.getArrivalAt(), numRecyclers, capacity, harvestedMetal, harvestedCrystal, remainingMetal,
          remainingCrystal);

      debrisField.setMetal(remainingMetal);
      debrisField.setCrystal(remainingCrystal);
      debrisField.setUpdatedAt(flight.getArrivalAt());
      debrisFieldRepository.save(debrisField);

      flightResources.setMetal(flightResources.getMetal() + harvestedMetal);
      flightResources.setCrystal(flightResources.getCrystal() + harvestedCrystal);
      flightRepository.save(flight);

      reportServiceInternal.createHarvestReport(flight, numRecyclers, capacity, harvestedMetal, harvestedCrystal,
          remainingMetal, remainingCrystal);
    }

    scheduleReturn(flight);
  }

  private void handleHold(Flight flight, Date at) {
    if (at.toInstant().getEpochSecond() == flight.getArrivalAt().toInstant().getEpochSecond()) {
      logger.info("Hold started: flightId={} startUserId={} startBodyId={} targetUserId={} targetBodyId={}" +
              " arrivalAt='{}' holdUntil='{}'",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetUser().getId(),
          flight.getTargetBody().getId(), flight.getArrivalAt(), flight.getHoldUntil());
      Event event = new Event();
      event.setAt(flight.getHoldUntil());
      event.setKind(EventKind.FLIGHT);
      event.setParam(flight.getId());
      eventScheduler.schedule(event);
    } else {
      logger.info("Hold ended: flightId={} startUserId={} startBodyId={} targetUserId={} targetBodyId={}" +
              " arrivalAt='{}' holdUntil='{}'",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetUser().getId(),
          flight.getTargetBody().getId(), flight.getArrivalAt(), flight.getHoldUntil());
      scheduleReturn(flight);
    }
  }

  private void handleTransport(Flight flight) {
    Body body = flight.getTargetBody();
    Date arrivalAt = flight.getArrivalAt();
    Resources resources = flight.getResources();

    // Create activity on the starting as well on the target body.
    long arrival = arrivalAt.toInstant().getEpochSecond();
    activityService.handleBodyActivity(flight.getStartBody().getId(), arrival);
    activityService.handleBodyActivity(body.getId(), arrival);

    flight.setResources(new Resources());
    bodyServiceInternal.updateResources(body, arrivalAt);
    body.getResources().add(resources);

    if (flight.getStartUser().getId() == body.getUser().getId()) {
      logger.info("Own transport: flightId={} startUserId={} startBodyId={} targetBodyId={} arrivalAt='{}'" +
              " resources={}",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetBody().getId(),
          flight.getArrivalAt(), resources);
      reportServiceInternal.createTransportReport(flight, flight.getStartUser(), flight.getStartUser(), resources);
    } else {
      logger.info("Foreign transport: flightId={} startUserId={} startBodyId={} targetUserId={} targetBodyId={}" +
              " arrivalAt='{}' resources={}",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetUser().getId(),
          flight.getTargetBody().getId(), arrivalAt, resources);
      reportServiceInternal.createTransportReport(flight, flight.getStartUser(), body.getUser(), resources);
      reportServiceInternal.createTransportReport(flight, body.getUser(), flight.getStartUser(), resources);
    }

    scheduleReturn(flight);
  }

  private void handleMissileAttack(Flight flight) {
    Body body = flight.getTargetBody();

    // Create activity.
    activityService.handleBodyActivity(body.getId(), flight.getArrivalAt().toInstant().getEpochSecond());

    FlightUnit missiles = flight.getUnits().get(UnitKind.INTERPLANETARY_MISSILE);
    if (missiles == null) {
      logger.error("Missile attack, no missiles: flightId={} startUserId={} startBodyId={} targetUserId={}" +
              " targetBodyId={} arrivalAt='{}'",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetUser().getId(),
          body.getId(), flight.getArrivalAt());
      flightRepository.delete(flight);
      return;
    }
    int numMissiles = missiles.getCount();

    BodyUnit antiMissiles;
    if (body.getCoordinates().getKind() == CoordinatesKind.PLANET) {
      antiMissiles = body.getUnits().get(UnitKind.ANTI_BALLISTIC_MISSILE);
    } else {
      assert body.getCoordinates().getKind() == CoordinatesKind.MOON;
      // Anti-ballistic missiles from planet defend the moon.
      Coordinates moonCoords = body.getCoordinates();
      Coordinates planetCoords = new Coordinates(moonCoords.getGalaxy(), moonCoords.getSystem(),
          moonCoords.getPosition(), CoordinatesKind.PLANET);
      Optional<Body> planet = bodyRepository.findByCoordinates(planetCoords);
      if (!planet.isPresent()) {
        logger.error("Missile attack, moon without planet: flightId={} startUserId={} startBodyId={} targetUserId={}" +
                " targetBodyId={} arrivalAt='{}'",
            flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(),
            flight.getTargetUser().getId(), body.getId(), flight.getArrivalAt());
        flightUnitRepository.delete(missiles);
        flightRepository.delete(flight);
        return;
      }
      antiMissiles = planet.get().getUnits().get(UnitKind.ANTI_BALLISTIC_MISSILE);
    }

    if (antiMissiles != null) {
      int count = antiMissiles.getCount();
      int n = Math.min(numMissiles, count);
      numMissiles -= n;
      count -= n;
      if (count == 0) {
        bodyUnitRepository.delete(antiMissiles);
      } else {
        antiMissiles.setCount(count);
      }
    }

    if (numMissiles == 0) {
      logger.info("Missile attack, missiles destroyed: flightId={} startUserId={} startBodyId={} targetUserId={}" +
              " targetBodyId={} arrivalAt='{}'",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetUser().getId(),
          body.getId(), flight.getArrivalAt());
      flightUnitRepository.delete(missiles);
      flightRepository.delete(flight);
      reportServiceInternal.createMissileAttackReport(flight, 0);
      return;
    }

    Map<UnitKind, UnitItem> defense = UnitItem.getDefense();
    Map<UnitKind, BodyUnit> units = body.getUnits().entrySet().stream()
        .filter(e -> {
          UnitKind kind = e.getKey();
          return defense.containsKey(kind) && kind != UnitKind.ANTI_BALLISTIC_MISSILE &&
              kind != UnitKind.INTERPLANETARY_MISSILE;
        })
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (a, b) -> {
              throw new IllegalStateException();
            },
            () -> new EnumMap<>(UnitKind.class)
        ));

    ArrayList<UnitKind> order = new ArrayList<>(units.keySet());
    Collections.shuffle(order, ThreadLocalRandom.current());

    if (logger.isInfoEnabled()) {
      String orderString = order.stream()
          .map(UnitKind::toString)
          .collect(Collectors.joining(", "));
      logger.info("Missile attack, destroying defense: flightId={} startUserId={} startBodyId={} targetUserId={}" +
              " targetBodyId={} arrivalAt='{}' numMissiles={} order='{}'",
          flight.getId(), flight.getStartUser().getId(), flight.getStartBody().getId(), flight.getTargetUser().getId(),
          body.getId(), flight.getArrivalAt(), numMissiles, orderString);
    }

    final double defFactor = 0.1 + 0.01 * flight.getTargetUser().getTechnologyLevel(TechnologyKind.ARMOR_TECHNOLOGY);
    double power = numMissiles * defense.get(UnitKind.INTERPLANETARY_MISSILE).getBaseWeapons() *
        (1.0 + 0.1 * flight.getStartUser().getTechnologyLevel(TechnologyKind.WEAPONS_TECHNOLOGY));
    int totalDestroyed = 0;

    for (Iterator<UnitKind> it = order.iterator(); it.hasNext() && power > 0.0; ) {
      UnitKind kind = it.next();

      BodyUnit unit = units.get(kind);
      assert unit != null;
      int count = unit.getCount();

      double armor = defFactor * defense.get(kind).getBaseArmor();
      int numDestroyed = Math.min(count, (int) (power / armor));
      power -= armor * numDestroyed;

      if (numDestroyed == count) {
        bodyUnitRepository.delete(unit);
      } else {
        unit.setCount(count - numDestroyed);
      }

      totalDestroyed += numDestroyed;
    }

    flightUnitRepository.delete(missiles);
    flightRepository.delete(flight);

    reportServiceInternal.createMissileAttackReport(flight, totalDestroyed);
  }

  private void deployUnits(Flight flight, Body body) {
    Map<UnitKind, BodyUnit> bodyUnits = body.getUnits();
    for (Map.Entry<UnitKind, FlightUnit> entry : flight.getUnits().entrySet()) {
      UnitKind kind = entry.getKey();
      int count = entry.getValue().getCount();
      if (count == 0) {
        continue;
      }
      BodyUnit bodyUnit = bodyUnits != null ? bodyUnits.get(kind) : null;
      if (bodyUnit == null) {
        BodyUnitKey key = new BodyUnitKey();
        key.setBody(body);
        key.setKind(kind);
        bodyUnit = new BodyUnit();
        bodyUnit.setKey(key);
        bodyUnit.setCount(count);
      } else {
        bodyUnit.setCount(bodyUnit.getCount() + count);
      }
      bodyUnitRepository.save(bodyUnit);
    }
    flightUnitRepository.deleteAll(flight.getUnits().values());
  }

  private List<Flight> getHoldingFlights(Body body, Date at) {
    PageRequest pageRequest = PageRequest.of(0, MAX_COMBATANTS - 1);
    return flightRepository.findHoldingFlights(body, at, pageRequest);
  }

  private void scheduleReturn(Flight flight) {
    Event event = new Event();
    event.setAt(flight.getReturnAt());
    event.setKind(EventKind.FLIGHT);
    event.setParam(flight.getId());
    eventScheduler.schedule(event);
  }
}
