package com.github.retro_game.retro_game.service.impl.missionhandler;

import com.github.retro_game.retro_game.battleengine.BattleEngine;
import com.github.retro_game.retro_game.battleengine.Combatant;
import com.github.retro_game.retro_game.battleengine.CombatantOutcome;
import com.github.retro_game.retro_game.battleengine.UnitGroupStats;
import com.github.retro_game.retro_game.dto.MoonCreationResultDto;
import com.github.retro_game.retro_game.dto.MoonDestructionResultDto;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.Item;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.repository.*;
import com.github.retro_game.retro_game.service.ActivityService;
import com.github.retro_game.retro_game.service.impl.BodyServiceInternal;
import com.github.retro_game.retro_game.service.impl.CombatReportServiceInternal;
import com.github.retro_game.retro_game.service.impl.ReportServiceInternal;
import io.vavr.Function3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AttackMissionHandler {
  private static final int MAX_COMBATANTS = 64;
  private static final Logger logger = LoggerFactory.getLogger(AttackMissionHandler.class);

  // Missiles don't participate in battles.
  private static final UnitKind[] fightUnitKinds = Arrays.stream(UnitKind.values())
      .filter(kind -> kind != UnitKind.ANTI_BALLISTIC_MISSILE && kind != UnitKind.INTERPLANETARY_MISSILE)
      .toArray(UnitKind[]::new);

  private final double fleetRebuildFactor;
  private final double defenseRebuildFactor;
  private final double fleetDebrisFactor;
  private final double defenseDebrisFactor;
  private final double maxMoonChance;
  private final BattleEngine battleEngine;
  private final BodyRepository bodyRepository;
  private final DebrisFieldRepository debrisFieldRepository;
  private final EventRepository eventRepository;
  private final FlightRepository flightRepository;
  private final PartyRepository partyRepository;
  private ActivityService activityService;
  private BodyServiceInternal bodyServiceInternal;
  private CombatReportServiceInternal combatReportServiceInternal;
  private ReportServiceInternal reportServiceInternal;
  private MissionHandlerUtils missionHandlerUtils;

  public AttackMissionHandler(@Value("${retro-game.fleet-rebuild-factor:0.0}") double fleetRebuildFactor,
                              @Value("${retro-game.defense-rebuild-factor:0.7}") double defenseRebuildFactor,
                              @Value("${retro-game.fleet-debris-factor:0.3}") double fleetDebrisFactor,
                              @Value("${retro-game.defense-debris-factor:0.0}") double defenseDebrisFactor,
                              @Value("${retro-game.max-moon-chance:0.2}") double maxMoonChance,
                              BattleEngine battleEngine, BodyRepository bodyRepository,
                              DebrisFieldRepository debrisFieldRepository, EventRepository eventRepository,
                              FlightRepository flightRepository, PartyRepository partyRepository) {
    this.fleetRebuildFactor = fleetRebuildFactor;
    this.defenseRebuildFactor = defenseRebuildFactor;
    this.fleetDebrisFactor = fleetDebrisFactor;
    this.defenseDebrisFactor = defenseDebrisFactor;
    this.maxMoonChance = maxMoonChance;
    this.battleEngine = battleEngine;
    this.bodyRepository = bodyRepository;
    this.debrisFieldRepository = debrisFieldRepository;
    this.eventRepository = eventRepository;
    this.flightRepository = flightRepository;
    this.partyRepository = partyRepository;
  }

  @Autowired
  public void setActivityService(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Autowired
  public void setCombatReportServiceInternal(CombatReportServiceInternal combatReportServiceInternal) {
    this.combatReportServiceInternal = combatReportServiceInternal;
  }

  @Autowired
  public void setReportServiceInternal(ReportServiceInternal reportServiceInternal) {
    this.reportServiceInternal = reportServiceInternal;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Autowired
  public void setMissionHandlerUtils(MissionHandlerUtils missionHandlerUtils) {
    this.missionHandlerUtils = missionHandlerUtils;
  }

  private record Loss(Resources fleet, Resources defense) {
    private static Loss zero() {
      return new Loss(new Resources(), new Resources());
    }

    private void add(Loss other) {
      fleet.add(other.fleet);
      defense.add(other.defense);
    }

    private Resources total() {
      var res = new Resources(fleet);
      res.add(defense);
      return res;
    }
  }

  private record LossAlivePair(Loss loss, boolean alive) {
  }

  public void handle(Flight mainFlight, boolean destroy) {
    var targetBody = mainFlight.getTargetBody();

    // Update resources and shipyard, and create activity.
    bodyServiceInternal.updateResourcesAndShipyard(targetBody, mainFlight.getArrivalAt());
    activityService.handleBodyActivity(targetBody.getId(), mainFlight.getArrivalAt().toInstant().getEpochSecond());

    // Get the flights participating in the battle.
    var attackersFlights = getAttackersFlights(mainFlight);
    var defendersFlights = getDefendersFlights(mainFlight);

    // Delete the party if any.
    var party = mainFlight.getParty();
    var partyId = 0L;
    if (party != null) {
      partyId = party.getId();
      partyRepository.delete(party);
      for (var flight : attackersFlights) {
        flight.setParty(null);
      }
    }

    // Make the combatants.
    var attackers = new ArrayList<Combatant>(attackersFlights.size());
    for (var flight : attackersFlights) {
      attackers.add(makeCombatantFromFlight(flight));
    }
    var defenders = new ArrayList<Combatant>(defendersFlights.size() + 1);
    defenders.add(makeCombatantFromBody(targetBody));
    for (var flight : defendersFlights) {
      defenders.add(makeCombatantFromFlight(flight));
    }

    // Fight.
    var seed = ThreadLocalRandom.current().nextInt();
    var startTime = System.nanoTime();
    var battleOutcome = battleEngine.fight(attackers, defenders, seed);
    var executionTime = System.nanoTime() - startTime;

    assert battleOutcome.numRounds() >= 1;
    Function<CombatantOutcome, EnumMap<UnitKind, UnitGroupStats>> getLastRoundStats =
        (CombatantOutcome outcome) -> outcome.getNthRoundUnitGroupsStats(battleOutcome.numRounds() - 1);
    Function<Integer, EnumMap<UnitKind, UnitGroupStats>> getAttackerStats =
        (Integer index) -> getLastRoundStats.apply(battleOutcome.attackersOutcomes().get(index));
    Function<Integer, EnumMap<UnitKind, UnitGroupStats>> getDefenderStats =
        (Integer index) -> getLastRoundStats.apply(battleOutcome.defendersOutcomes().get(index));

    // Rebuild and delete destroyed units, and calculate the loss.
    var attackersLoss = Loss.zero();
    var defendersLoss = Loss.zero();
    var attackersAlive = false;
    var defendersAlive = false;
    for (var i = 0; i < attackersFlights.size(); i++) {
      var pair = rebuildAndDeleteDestroyedUnits(attackersFlights.get(i).getUnitsArray(), getAttackerStats.apply(i));
      attackersLoss.add(pair.loss);
      attackersAlive |= pair.alive;
    }
    {
      var pair = rebuildAndDeleteDestroyedUnits(targetBody.getUnitsArray(), getDefenderStats.apply(0));
      defendersLoss.add(pair.loss);
      defendersAlive = pair.alive;
    }
    for (var i = 0; i < defendersFlights.size(); i++) {
      var pair = rebuildAndDeleteDestroyedUnits(defendersFlights.get(i).getUnitsArray(), getDefenderStats.apply(i + 1));
      defendersLoss.add(pair.loss);
      defendersAlive |= pair.alive;
    }

    var totalAttackersLoss = attackersLoss.total();
    var totalDefendersLoss = defendersLoss.total();

    BattleResult battleResult;
    if (attackersAlive && defendersAlive) {
      battleResult = BattleResult.DRAW;
    } else if (attackersAlive) {
      battleResult = BattleResult.ATTACKERS_WIN;
    } else {
      battleResult = BattleResult.DEFENDERS_WIN;
    }

    var debris = calcDebris(attackersLoss, defendersLoss);
    createOrUpdateDebrisField(mainFlight, debris);

    var moonChance = calcMoonChance(mainFlight, debris);
    var moonCreated = maybeCreateMoon(mainFlight, moonChance);
    var moonCreationRes = new MoonCreationResultDto(moonChance, moonCreated);

    // Try to destroy the moon.
    MoonDestructionResultDto moonDestructionRes = null;
    if (battleResult == BattleResult.ATTACKERS_WIN && destroy &&
        targetBody.getCoordinates().getKind() == CoordinatesKind.MOON) {
      var moonDestructionChance = calcMoonDestructionChance(targetBody, attackersFlights);
      var moonDestroyed = maybeDestroyMoon(targetBody, moonDestructionChance);
      var deathStarsDestructionChance = calcDeathStarsDestructionChance(targetBody);
      var deathStarsDestroyed = maybeDestroyDeathStars(attackersFlights, deathStarsDestructionChance);
      moonDestructionRes =
          new MoonDestructionResultDto(moonDestructionChance, moonDestroyed, deathStarsDestructionChance,
              deathStarsDestroyed);
    }

    // Plunder the body.
    var plunder = battleResult == BattleResult.ATTACKERS_WIN ? plunder(targetBody, attackersFlights) : new Resources();

    // Generate reports.
    var combatReport =
        combatReportServiceInternal.create(mainFlight.getArrivalAt(), attackers, defenders, battleOutcome, battleResult,
            totalAttackersLoss, totalDefendersLoss, plunder, debris, moonCreationRes, moonDestructionRes, seed,
            executionTime);
    var sent = new HashSet<Long>();
    Function3<User, Boolean, User, Integer> createSimplifiedReport = (User user, Boolean isAttacker, User enemy) -> {
      if (!sent.contains(user.getId())) {
        reportServiceInternal.createSimplifiedCombatReport(user, isAttacker, mainFlight.getArrivalAt(), enemy,
            targetBody.getCoordinates(), battleResult, battleOutcome.numRounds(), totalAttackersLoss,
            totalDefendersLoss, plunder, debris, moonCreationRes, combatReport);
        sent.add(user.getId());
      }
      return 0;
    };
    for (var flight : attackersFlights) {
      createSimplifiedReport.apply(flight.getStartUser(), true, mainFlight.getTargetUser());
    }
    sent.clear(); // If the same user is on both sides, send the report again.
    createSimplifiedReport.apply(targetBody.getUser(), false, mainFlight.getStartUser());
    for (var flight : defendersFlights) {
      createSimplifiedReport.apply(flight.getStartUser(), false, mainFlight.getStartUser());
    }

    // Make a log entry.
    if (logger.isInfoEnabled()) {
      var moonDestroyed = moonDestructionRes != null && moonDestructionRes.moonDestroyed();
      var deathStarsDestroyed = moonDestructionRes != null && moonDestructionRes.deathStarsDestroyed();

      Function<Stream<Flight>, String> makeFlightList =
          flights -> flights.map(f -> String.valueOf(f.getId())).collect(Collectors.joining(","));
      var partyFlights = makeFlightList.apply(attackersFlights.stream().skip(1));
      var holdingFlights = makeFlightList.apply(defendersFlights.stream());

      Function<List<Combatant>, String> makeUserList =
          combatants -> combatants.stream().skip(1).map(Combatant::userId).distinct().map(String::valueOf)
              .collect(Collectors.joining(","));
      var partyUsers = makeUserList.apply(attackers);
      var holdingUsers = makeUserList.apply(defenders);

      logger.info("Attack: flight={} arrivalAt='{}' startUser={} startBody={} start={} targetUser={} targetBody={} " +
              "target={} party={} partyFlights=[{}] partyUsers=[{}] holdingFlights=[{}] holdingUsers=[{}] " +
              "result={} attackersLoss={} defendersLoss={} debris={} plunder={} moonCreated={} destroy={} " +
              "moonDestroyed={} deathStarsDestroyed={} seed={} executionTime={} combatReport={}", mainFlight.getId(),
          mainFlight.getArrivalAt(), mainFlight.getStartUser().getId(), mainFlight.getStartBody().getId(),
          mainFlight.getStartBody().getCoordinates(), targetBody.getUser().getId(), targetBody.getId(),
          targetBody.getCoordinates(), partyId, partyFlights, partyUsers, holdingFlights, holdingUsers, battleResult,
          totalAttackersLoss, totalDefendersLoss, debris, plunder, moonCreationRes.created(), destroy, moonDestroyed,
          deathStarsDestroyed, seed, executionTime, combatReport.getId());
    }

    // Delete flights with no units and schedule returns for the alive fleets.
    var deletedFlights = new HashSet<Long>();
    for (var flight : attackersFlights) {
      if (flight.getTotalUnitsCount() == 0) {
        deletedFlights.add(flight.getId());
        flightRepository.delete(flight);
      } else {
        missionHandlerUtils.scheduleReturn(flight);
      }
    }
    for (var flight : defendersFlights) {
      if (flight.getTotalUnitsCount() == 0) {
        deletedFlights.add(flight.getId());
        flightRepository.delete(flight);
      }
    }

    // Remove the events of the now dead fleets.
    var events = eventRepository.findByKindAndParamIn(EventKind.FLIGHT, deletedFlights);
    eventRepository.deleteAll(events);
  }

  private List<Flight> getAttackersFlights(Flight flight) {
    var party = flight.getParty();
    if (party == null) {
      return Collections.singletonList(flight);
    }

    var flights = flightRepository.findByPartyOrderById(party);
    // It shouldn't be possible to add a fleet to the party if the party is full, thus we can have at most
    // MAX_COMBATANTS.
    assert flights.size() >= 1 && flights.size() <= MAX_COMBATANTS;
    // The main fleet should always be the first one.
    assert flights.get(0).getId() == flight.getId();
    return flights;
  }

  private List<Flight> getDefendersFlights(Flight flight) {
    // Get the flights that are still holding on the body. We fetch only MAX_COMBATANTS - 1 flights, since we consider
    // also the target body in the battle.
    var pageRequest = PageRequest.of(0, MAX_COMBATANTS - 1);
    return flightRepository.findHoldingFlights(flight.getTargetBody(), flight.getArrivalAt(), pageRequest);
  }

  private static Combatant makeCombatantFromBody(Body body) {
    return makeCombatant(body.getUser(), body.getCoordinates(), body.getUnitsArray());
  }

  private static Combatant makeCombatantFromFlight(Flight flight) {
    var coordinates = flight.getStartBody().getCoordinates();
    return makeCombatant(flight.getStartUser(), coordinates, flight.getUnitsArray());
  }

  private static Combatant makeCombatant(User user, Coordinates coordinates, int[] units) {
    var weaponsTech = user.getTechnologyLevel(TechnologyKind.WEAPONS_TECHNOLOGY);
    var shieldingTech = user.getTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY);
    var armorTech = user.getTechnologyLevel(TechnologyKind.ARMOR_TECHNOLOGY);
    assert weaponsTech >= 0 && shieldingTech >= 0 && armorTech >= 0;
    var unitGroups = makeUnitGroups(units);
    return new Combatant(user.getId(), coordinates, weaponsTech, shieldingTech, armorTech, unitGroups);
  }

  private static EnumMap<UnitKind, Long> makeUnitGroups(int[] units) {
    assert units.length == UnitKind.values().length;
    var groups = new EnumMap<UnitKind, Long>(UnitKind.class);
    for (var kind : fightUnitKinds) {
      var count = units[kind.ordinal()];
      assert count >= 0;
      if (count == 0) {
        continue;
      }
      groups.put(kind, (long) count);
    }
    return groups;
  }

  private LossAlivePair rebuildAndDeleteDestroyedUnits(int[] units, Map<UnitKind, UnitGroupStats> lastRoundStats) {
    var fleetLoss = new Resources();
    var defenseLoss = new Resources();
    var alive = false;

    for (var kind : fightUnitKinds) {
      var numBeforeBattle = (long) units[kind.ordinal()];
      var numAfterBattle = lastRoundStats.get(kind).numRemainingUnits();
      assert numBeforeBattle >= 0 && numAfterBattle >= 0 && numAfterBattle <= numBeforeBattle;

      if (numAfterBattle > 0) {
        alive = true;
      }

      var numLost = numBeforeBattle - numAfterBattle;

      var isFleet = UnitItem.getFleet().containsKey(kind);
      var rebuildFactor = isFleet ? fleetRebuildFactor : defenseRebuildFactor;
      var numRebuilt = calcNumRebuiltUnits(numLost, rebuildFactor);

      var numAfterRebuild = numAfterBattle + numRebuilt;
      assert numAfterRebuild <= numBeforeBattle;

      units[kind.ordinal()] = (int) numAfterRebuild;

      var numLostAfterRebuild = numBeforeBattle - numAfterRebuild;
      var cost = Item.get(kind).getCost();
      cost.mul(numLostAfterRebuild);
      var loss = isFleet ? fleetLoss : defenseLoss;
      loss.add(cost);
    }

    var loss = new Loss(fleetLoss, defenseLoss);
    return new LossAlivePair(loss, alive);
  }

  // The number of rebuilt units is binomially distributed random variable, where:
  // mean = numLost * factor
  // variance = numLost * factor * (1 - factor)
  // We approximate it using the normal distribution.
  private long calcNumRebuiltUnits(long numLost, double factor) {
    assert numLost >= 0;
    assert factor >= 0.0 && factor <= 1.0;
    // factor 0.0 is typical for fleets, don't waste time calculating.
    if (factor == 0.0) {
      return 0;
    }
    var mean = numLost * factor;
    var sd = Math.sqrt(numLost * factor * (1.0 - factor));
    var numRebuilt = (long) ThreadLocalRandom.current().nextGaussian(mean, sd);
    if (numRebuilt > numLost) {
      return numLost;
    }
    if (numRebuilt < 0) {
      return 0;
    }
    return numRebuilt;
  }

  private Resources calcDebris(Loss attackersLoss, Loss defendersLoss) {
    var totalLoss = Loss.zero();
    totalLoss.add(attackersLoss);
    totalLoss.add(defendersLoss);
    totalLoss.fleet.mul(fleetDebrisFactor);
    totalLoss.defense.mul(defenseDebrisFactor);
    var debris = totalLoss.fleet;
    debris.add(totalLoss.defense);
    debris.setDeuterium(0.0);
    debris.floor();
    assert debris.isNonNegative();
    return debris;
  }

  private void createOrUpdateDebrisField(Flight mainFlight, Resources debris) {
    var metal = (long) debris.getMetal();
    var crystal = (long) debris.getCrystal();
    if (metal == 0 && crystal == 0) {
      // Nothing to do.
      return;
    }

    var coords = mainFlight.getTargetCoordinates();
    var key = new DebrisFieldKey(coords.getGalaxy(), coords.getSystem(), coords.getPosition());
    var dfOpt = debrisFieldRepository.findById(key);
    if (dfOpt.isEmpty()) {
      // Create a new debris field.
      var df = new DebrisField(key, mainFlight.getArrivalAt(), mainFlight.getArrivalAt(), metal, crystal);
      debrisFieldRepository.save(df);
    } else {
      // Update the existing debris field.
      var df = dfOpt.get();
      df.setUpdatedAt(mainFlight.getArrivalAt());
      df.setMetal(df.getMetal() + metal);
      df.setCrystal(df.getCrystal() + crystal);
    }
  }

  private double calcMoonChance(Flight mainFlight, Resources debris) {
    // If the battle is already on the moon, there is no chance to create another.
    if (mainFlight.getTargetCoordinates().getKind() == CoordinatesKind.MOON) {
      return 0.0;
    }

    // Each 100k resources is 1%.
    return Math.min(maxMoonChance, 1e-7 * (debris.getMetal() + debris.getCrystal()));
  }

  private boolean maybeCreateMoon(Flight mainFlight, double moonChance) {
    // Don't try to create a moon if the chance is below 1%.
    if (moonChance < 0.01) {
      return false;
    }

    // Check if we are lucky!
    var r = ThreadLocalRandom.current().nextDouble();
    if (moonChance < r) {
      return false;
    }

    // Check if the moon already exists.
    var targetCoords = mainFlight.getTargetCoordinates();
    var moonCoords = new Coordinates(targetCoords.getGalaxy(), targetCoords.getSystem(), targetCoords.getPosition(),
        CoordinatesKind.MOON);
    var exists = bodyRepository.existsByCoordinates(moonCoords);
    if (exists) {
      return false;
    }

    // Create a moon.
    bodyServiceInternal.createMoon(mainFlight.getTargetUser(), moonCoords, mainFlight.getArrivalAt(), moonChance);
    return true;
  }

  private static double calcMoonDestructionChance(Body moon, List<Flight> flights) {
    var diameter = moon.getDiameter();
    var numDeathStars = flights.stream().mapToLong(f -> f.getUnitsCount(UnitKind.DEATH_STAR)).sum();
    return Math.min(1.0, (1.0 - 0.01 * Math.sqrt(diameter)) * Math.sqrt(numDeathStars));
  }

  private boolean maybeDestroyMoon(Body moon, double moonDestructionChance) {
    var r = ThreadLocalRandom.current().nextDouble();
    if (moonDestructionChance < r) {
      return false;
    }

    // Find the associated planet.
    var moonCoords = moon.getCoordinates();
    var planetCoords = new Coordinates(moonCoords.getGalaxy(), moonCoords.getSystem(), moonCoords.getPosition(),
        CoordinatesKind.PLANET);
    var planetOpt = bodyRepository.findByCoordinates(planetCoords);
    if (planetOpt.isEmpty()) {
      // This shouldn't happen.
      logger.error("Moon destruction failed, moon exists without the associated planet: moonId={} moonCoords={}",
          moon.getId(), moonCoords);
      return false;
    }
    var planet = planetOpt.get();

    // The flights targeting the moon are rerouted to the associated planet.
    for (var flight : flightRepository.findByStartBody(moon)) {
      flight.setStartBody(planet);
    }
    for (var flight : flightRepository.findByTargetBody(moon)) {
      flight.setTargetBody(planet);
      flight.getTargetCoordinates().setKind(CoordinatesKind.PLANET);
    }

    // Reroute parties as well.
    for (var party : partyRepository.findByTargetBody(moon)) {
      party.setTargetBody(planet);
      party.getTargetCoordinates().setKind(CoordinatesKind.PLANET);
    }

    bodyServiceInternal.destroyMoon(moon);
    return true;
  }

  private static double calcDeathStarsDestructionChance(Body moon) {
    var diameter = moon.getDiameter();
    assert diameter <= 10000;
    var chance = 5e-05 * diameter;
    assert chance <= 1.0;
    return chance;
  }

  private static boolean maybeDestroyDeathStars(List<Flight> flights, double deathStarsDestructionChance) {
    var r = ThreadLocalRandom.current().nextDouble();
    if (deathStarsDestructionChance < r) {
      return false;
    }

    for (var flight : flights) {
      flight.setUnitsCount(UnitKind.DEATH_STAR, 0);
    }
    return true;
  }

  private static class FlightPlunderState {
    private final Flight flight;
    private final Resources plunder;
    private long capacity;

    public FlightPlunderState(Flight flight, Resources plunder, long capacity) {
      this.flight = flight;
      this.plunder = plunder;
      this.capacity = capacity;
    }
  }

  private Resources plunder(Body body, List<Flight> flights) {
    // The maximal amount of resources that can be taken.
    var remaining = new Resources(body.getResources());
    remaining.mul(0.5);
    remaining.floor();

    var states = new FlightPlunderState[flights.size()];
    var i = 0;
    for (var flight : flights) {
      var capacity = calcCapacity(flight.getUnitsArray());
      capacity -= (long) Math.ceil(flight.getResources().total());
      capacity = Math.max(0L, capacity);
      states[i++] = new FlightPlunderState(flight, new Resources(), capacity);
    }

    // Sort in the order of increasing capacities. This allows a later fleet to take more resources if a previous fleet
    // cannot take its part.
    Arrays.sort(states, Comparator.comparing(s -> s.capacity));

    // 1. Fill 1/3 of the capacity with metal.
    // 2. Fill 1/2 of the capacity with crystal.
    // 3. Fill the remaining capacity with deuterium.
    // 4. Fill 1/2 of the capacity with metal.
    // 5. Fill 1/2 of the capacity with crystal.
    takeResources(remaining, states, 3, Resources::getMetal, Resources::setMetal);
    takeResources(remaining, states, 2, Resources::getCrystal, Resources::setCrystal);
    takeResources(remaining, states, 1, Resources::getDeuterium, Resources::setDeuterium);
    takeResources(remaining, states, 2, Resources::getMetal, Resources::setMetal);
    takeResources(remaining, states, 2, Resources::getCrystal, Resources::setCrystal);

    // Calculate the total plunder and update flights.
    var totalPlunder = new Resources();
    for (var state : states) {
      assert state.capacity >= 0;
      var flight = state.flight;
      var r = flight.getResources();
      r.add(state.plunder);
      r.floor();
      totalPlunder.add(state.plunder);
    }
    totalPlunder.floor();

    // Update the body.
    body.getResources().sub(totalPlunder);
    assert body.getResources().isNonNegative();

    return totalPlunder;
  }

  private static void takeResources(Resources remaining, FlightPlunderState[] states, int factor,
                                    Function<Resources, Double> getter, BiConsumer<Resources, Double> setter) {
    for (var i = 0; i < states.length; i++) {
      var state = states[i];
      var rem = (long) (double) getter.apply(remaining);
      assert state.capacity >= 0 && rem >= 0;
      var taken = Math.min(state.capacity / factor, rem / (states.length - i));
      state.capacity -= taken;
      setter.accept(remaining, (double) (rem - taken));
      setter.accept(state.plunder, getter.apply(state.plunder) + taken);
    }
  }

  // TODO: Move somewhere else.
  private static long calcCapacity(int[] units) {
    var capacity = 0L;
    for (var kind : fightUnitKinds) {
      var count = units[kind.ordinal()];
      assert count >= 0;
      if (count == 0) {
        continue;
      }
      var item = Item.get(kind);
      capacity += (long) count * item.getCapacity();
    }
    return capacity;
  }
}
