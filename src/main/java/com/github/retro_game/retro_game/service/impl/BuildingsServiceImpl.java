package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.*;
import com.github.retro_game.retro_game.model.building.BuildingItem;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.repository.EventRepository;
import com.github.retro_game.retro_game.service.exception.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BuildingsServiceImpl implements BuildingsServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(BuildingsServiceImpl.class);
  private final int buildingQueueCapacity;
  private final int fieldsPerTerraformerLevel;
  private final int fieldsPerLunarBaseLevel;
  private final boolean allowNanitesOnMoon;
  private final ItemTimeUtils itemTimeUtils;
  private final BodyRepository bodyRepository;
  private final EventRepository eventRepository;
  private BodyServiceInternal bodyServiceInternal;
  private EventScheduler eventScheduler;

  private class State {
    final Map<BuildingKind, Integer> buildings;
    int usedFields;
    int maxFields;

    State(Body body, SortedMap<Integer, BuildingQueueEntry> queue) {
      buildings = new EnumMap<>(BuildingKind.class);
      for (var entry : body.getBuildings().entrySet()) {
        var level = entry.getValue();
        buildings.put(entry.getKey(), level);
        usedFields += level;
      }
      maxFields = bodyServiceInternal.getMaxFields(body, buildings);
      if (queue != null) {
        for (BuildingQueueEntry entry : queue.values()) {
          if (entry.action() == BuildingQueueAction.CONSTRUCT) {
            construct(entry.kind());
          } else {
            assert entry.action() == BuildingQueueAction.DESTROY;
            destroy(entry.kind());
          }
        }
      }
    }

    void construct(BuildingKind kind) {
      buildings.put(kind, buildings.getOrDefault(kind, 0) + 1);
      usedFields++;
      if (kind == BuildingKind.TERRAFORMER) {
        maxFields += fieldsPerTerraformerLevel;
      } else if (kind == BuildingKind.LUNAR_BASE) {
        maxFields += fieldsPerLunarBaseLevel;
      }
    }

    void destroy(BuildingKind kind) {
      // A terraformer and lunar base cannot be destroyed once built.
      assert kind != BuildingKind.TERRAFORMER && kind != BuildingKind.LUNAR_BASE;
      assert buildings.containsKey(kind) && buildings.get(kind) >= 1;
      buildings.put(kind, buildings.get(kind) - 1);
      usedFields--;
    }
  }

  public BuildingsServiceImpl(@Value("${retro-game.building-queue-capacity}") int buildingQueueCapacity,
                              @Value("${retro-game.fields-per-terraformer-level}") int fieldsPerTerraformerLevel,
                              @Value("${retro-game.fields-per-lunar-base-level}") int fieldsPerLunarBaseLevel,
                              @Value("${retro-game.allow-nanites-on-moons}") boolean allowNanitesOnMoon,
                              ItemTimeUtils itemTimeUtils,
                              BodyRepository bodyRepository,
                              EventRepository eventRepository) {
    this.buildingQueueCapacity = buildingQueueCapacity;
    this.fieldsPerTerraformerLevel = fieldsPerTerraformerLevel;
    this.fieldsPerLunarBaseLevel = fieldsPerLunarBaseLevel;
    this.allowNanitesOnMoon = allowNanitesOnMoon;
    this.itemTimeUtils = itemTimeUtils;
    this.bodyRepository = bodyRepository;
    this.eventRepository = eventRepository;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Autowired
  public void setEventScheduler(EventScheduler eventScheduler) {
    this.eventScheduler = eventScheduler;
  }

  @PostConstruct
  private void checkProperties() {
    Assert.isTrue(buildingQueueCapacity >= 1,
        "retro-game.building-queue-capacity must be at least 1");
    Assert.isTrue(fieldsPerTerraformerLevel > 1,
        "retro-game.fields-per-terraformer-level must be greater than 1");
    Assert.isTrue(fieldsPerLunarBaseLevel > 1,
        "retro-game.fields-per-lunar-base-level must be greater than 1");
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
  public BuildingsAndQueuePairDto getBuildingsAndQueuePair(long bodyId) {
    var body = bodyServiceInternal.getUpdated(bodyId);
    var production = bodyServiceInternal.getProduction(body);
    var state = new State(body, null);
    var queue = getQueueAndUpdateState(state, body, production);
    var buildings = getBuildings(state, body, production, queue.size());
    return new BuildingsAndQueuePairDto(buildings, queue);
  }

  private static int getLevelWithAction(int curLevel, BuildingQueueAction action) {
    assert action == BuildingQueueAction.CONSTRUCT || action == BuildingQueueAction.DESTROY;
    var nextLevel = curLevel + (action == BuildingQueueAction.CONSTRUCT ? 1 : -1);
    assert nextLevel >= 0;
    return nextLevel;
  }

  private List<BuildingQueueEntryDto> getQueueAndUpdateState(State state, Body body, ProductionDto production) {
    var queue = body.getBuildingQueue();
    var ret = new ArrayList<BuildingQueueEntryDto>(queue.size());

    if (queue.size() == 0) {
      return ret;
    }

    var it = queue.entrySet().iterator();
    var next = it.next();
    var first = true;
    var finishAt = 0L;
    var upMovable = false;
    do {
      var cur = next;
      next = it.hasNext() ? it.next() : null;

      var curKind = cur.getValue().kind();
      var curAction = cur.getValue().action();

      var curLevelFrom = state.buildings.getOrDefault(curKind, 0);
      var curLevelTo = getLevelWithAction(curLevelFrom, curAction);
      var curCost = ItemCostUtils.getCost(curKind, curLevelTo);
      var curEnergy = ItemCostUtils.getRequiredEnergy(curKind, curLevelTo);

      if (first) {
        var event = eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, body.getId());
        Assert.isTrue(event.isPresent(), "Event must be present");
        finishAt = event.get().getAt().toInstant().getEpochSecond();
      } else {
        long requiredTime;
        if (curAction == BuildingQueueAction.CONSTRUCT) {
          requiredTime = getConstructionTime(curCost, state.buildings);
        } else {
          requiredTime = getDestructionTime(curCost, state.buildings);
        }
        finishAt += requiredTime;
      }

      // Check dependencies of the subsequent entries.
      var tail = queue.tailMap(cur.getKey());
      var downMovable = canSwapTop(state, tail);
      var cancelable = canRemoveTop(state, tail);

      // Moving down or cancelling the first entry is equivalent to building the second one, which is the reason
      // for checking resources.
      if (first && next != null) {
        var nextKind = next.getValue().kind();
        var nextAction = next.getValue().action();

        var nextLevelFrom = state.buildings.getOrDefault(nextKind, 0);
        var nextLevelTo = getLevelWithAction(nextLevelFrom, nextAction);
        var nextCost = ItemCostUtils.getCost(nextKind, nextLevelTo);
        var nextEnergy = ItemCostUtils.getRequiredEnergy(nextKind, nextLevelTo);

        // If we cancel the current building, we will have curCost more resources.
        nextCost.sub(curCost);

        if (!body.getResources().greaterOrEqual(nextCost) || nextEnergy > production.totalEnergy() ||
            !ItemRequirementsUtils.meetsTechnologiesRequirements(Item.get(nextKind), body.getUser())) {
          downMovable = cancelable = false;
        }
      }

      ret.add(new BuildingQueueEntryDto(Converter.convert(curKind), cur.getKey(), curLevelFrom, curLevelTo,
          Converter.convert(curCost), curEnergy, Date.from(Instant.ofEpochSecond(finishAt)), downMovable, upMovable,
          cancelable));

      if (curAction == BuildingQueueAction.CONSTRUCT) {
        state.construct(curKind);
      } else {
        state.destroy(curKind);
      }

      first = false;
      upMovable = downMovable;
    } while (next != null);

    return ret;
  }

  private List<BuildingDto> getBuildings(State state, Body body, ProductionDto production, int queueSize) {
    var buildings = new ArrayList<BuildingDto>(BuildingKindDto.values().length);

    for (var entry : BuildingItem.getAll().entrySet()) {
      var kind = entry.getKey();
      var item = entry.getValue();

      var currentLevel = body.getBuildingLevel(kind);
      var futureLevel = state.buildings.get(kind);

      var meetsRequirements = meetsSpecialRequirements(body, item, kind) &&
          ItemRequirementsUtils.meetsBuildingsRequirements(item, state.buildings) &&
          (queueSize > 0 || ItemRequirementsUtils.meetsTechnologiesRequirements(item, body.getUser()));

      // Show the building even if it doesn't meet the requirements.
      var show = futureLevel > 0 || meetsRequirements;
      if (!show) {
        continue;
      }

      var nextLevel = futureLevel + 1;
      var cost = ItemCostUtils.getCost(kind, nextLevel);
      var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, nextLevel);
      var constructionTime = getConstructionTime(cost, state.buildings);

      var missingResources = new Resources(cost);
      missingResources.sub(body.getResources());
      missingResources.max(0.0);
      var neededSmallCargoes = ItemUtils.calcNumUnitsForCapacity(UnitKind.SMALL_CARGO, missingResources);
      var neededLargeCargoes = ItemUtils.calcNumUnitsForCapacity(UnitKind.LARGE_CARGO, missingResources);
      var accumulationTime = ItemTimeUtils.calcAccumulationTime(body.getUpdatedAt(), missingResources, production);

      var hasEnoughFields = state.usedFields < state.maxFields;
      var isQueueNotFull = queueSize < buildingQueueCapacity;
      var hasEnoughResources = body.getResources().greaterOrEqual(cost);
      var hasEnoughEnergy = production.totalEnergy() >= requiredEnergy;
      var canConstructNow = hasEnoughFields && isQueueNotFull && meetsRequirements &&
          (queueSize > 0 || (hasEnoughResources && hasEnoughEnergy));

      buildings.add(
          new BuildingDto(Converter.convert(kind), currentLevel, futureLevel, Converter.convert(cost), requiredEnergy,
              constructionTime, Converter.convert(missingResources), neededSmallCargoes, neededLargeCargoes,
              accumulationTime, canConstructNow));
    }

    return buildings;
  }

  @Override
  public Map<BuildingKind, Tuple2<Integer, Integer>> getCurrentAndFutureLevels(Body body) {
    State state = new State(body, body.getBuildingQueue());
    return Arrays.stream(BuildingKind.values())
        .filter(kind -> body.getBuildingLevel(kind) != 0 || state.buildings.getOrDefault(kind, 0) != 0)
        .collect(Collectors.toMap(
            Function.identity(),
            kind -> Tuple.of(body.getBuildingLevel(kind), state.buildings.getOrDefault(kind, 0)),
            (a, b) -> {
              throw new IllegalStateException();
            },
            () -> new EnumMap<>(BuildingKind.class)
        ));
  }

  @Override
  public Optional<OngoingBuildingDto> getOngoingBuilding(Body body) {
    var buildingQueue = body.getBuildingQueue();
    if (buildingQueue.isEmpty()) {
      return Optional.empty();
    }
    var first = buildingQueue.get(buildingQueue.firstKey());
    assert first != null;
    var level = getLevelWithAction(body.getBuildingLevel(first.kind()), first.action());
    return Optional.of(new OngoingBuildingDto(first.kind(), level));
  }

  @Override
  public Optional<Date> getOngoingBuildingFinishAt(Body body) {
    return eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, body.getId()).map(Event::getAt);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void construct(long bodyId, BuildingKindDto kind) {
    var k = Converter.convert(kind);
    var body = bodyServiceInternal.getUpdated(bodyId);
    var queue = body.getBuildingQueue();

    if (queue.size() >= buildingQueueCapacity) {
      logger.info("Constructing building failed, queue is full: bodyId={} kind={}", bodyId, k);
      throw new QueueFullException();
    }

    var state = new State(body, queue);
    if (state.usedFields >= state.maxFields) {
      logger.info("Constructing building failed, no more free fields: bodyId={} kind={}", bodyId, k);
      throw new NoMoreFreeFieldsException();
    }

    var item = Item.get(k);
    if (!meetsSpecialRequirements(body, item, k) ||
        !ItemRequirementsUtils.meetsBuildingsRequirements(item, state.buildings) ||
        (queue.isEmpty() && !ItemRequirementsUtils.meetsTechnologiesRequirements(item, body.getUser()))) {
      logger.info("Constructing building failed, requirements not met: bodyId={} kind={}", bodyId, k);
      throw new RequirementsNotMetException();
    }

    var sequenceNumber = 1;
    if (!queue.isEmpty()) {
      sequenceNumber = queue.lastKey() + 1;
      logger.info("Constructing building successful, appending to queue: bodyId={} kind={} sequenceNumber={}",
          bodyId, k, sequenceNumber);
    } else {
      var level = state.buildings.getOrDefault(k, 0) + 1;

      var cost = ItemCostUtils.getCost(k, level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.info("Constructing building failed, not enough resources: bodyId={} kind={}", bodyId, k);
        throw new NotEnoughResourcesException();
      }
      body.getResources().sub(cost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(k, level);
      if (requiredEnergy > 0) {
        var totalEnergy = bodyServiceInternal.getProduction(body).totalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.info("Constructing building failed, not enough energy: bodyId={} kind={}", bodyId, k);
          throw new NotEnoughEnergyException();
        }
      }

      logger.info("Constructing building successful, creating a new event: bodyId={} kind={}", bodyId, k);
      var now = body.getUpdatedAt();
      var requiredTime = getConstructionTime(cost, state.buildings);
      var startAt = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      var event = new Event();
      event.setAt(startAt);
      event.setKind(EventKind.BUILDING_QUEUE);
      event.setParam(bodyId);
      eventScheduler.schedule(event);
    }

    var entry = new BuildingQueueEntry(k, BuildingQueueAction.CONSTRUCT);
    queue.put(sequenceNumber, entry);
    body.setBuildingQueue(queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void destroy(long bodyId, BuildingKindDto kind) {
    var k = Converter.convert(kind);
    var body = bodyServiceInternal.getUpdated(bodyId);
    var queue = body.getBuildingQueue();

    if (k == BuildingKind.TERRAFORMER || k == BuildingKind.LUNAR_BASE) {
      logger.info("Destroying building failed, cannot destroy this building: bodyId={} kind={}", bodyId, k);
      throw new WrongBuildingKindException();
    }

    if (queue.size() >= buildingQueueCapacity) {
      logger.info("Destroying building failed, queue is full: bodyId={} kind={}", bodyId, k);
      throw new QueueFullException();
    }

    var state = new State(body, queue);
    if (state.buildings.getOrDefault(k, 0) == 0) {
      logger.info("Destroying building failed, the building is already going to be fully destroyed: bodyId={} kind={}",
          bodyId, k);
      throw new BuildingAlreadyDestroyedException();
    }

    var sequenceNumber = 1;
    if (!queue.isEmpty()) {
      sequenceNumber = queue.lastKey() + 1;
      logger.info("Destroying building successful, appending to queue: bodyId={} kind={} sequenceNumber={}",
          bodyId, k, sequenceNumber);
    } else {
      assert state.buildings.containsKey(k);
      var level = state.buildings.get(k) - 1;
      assert level >= 0;

      var cost = ItemCostUtils.getCost(k, level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.info("Destroying building failed, not enough resources: bodyId={} kind={}", bodyId, k);
        throw new NotEnoughResourcesException();
      }
      body.getResources().sub(cost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(k, level);
      if (requiredEnergy > 0) {
        var totalEnergy = bodyServiceInternal.getProduction(body).totalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.info("Destroying building failed, not enough energy: bodyId={} kind={}", bodyId, k);
          throw new NotEnoughEnergyException();
        }
      }

      logger.info("Destroying building successful, create a new event: bodyId={} kind={}", bodyId, k);
      var now = body.getUpdatedAt();
      var requiredTime = getDestructionTime(cost, state.buildings);
      var startAt = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      var event = new Event();
      event.setAt(startAt);
      event.setKind(EventKind.BUILDING_QUEUE);
      event.setParam(bodyId);
      eventScheduler.schedule(event);
    }

    var entry = new BuildingQueueEntry(k, BuildingQueueAction.DESTROY);
    queue.put(sequenceNumber, entry);
    body.setBuildingQueue(queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void moveDown(long bodyId, int sequenceNumber) {
    var body = bodyServiceInternal.getUpdated(bodyId);
    var queue = body.getBuildingQueue();

    if (!queue.containsKey(sequenceNumber)) {
      logger.info("Moving down entry in building queue failed, no such queue entry: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    var head = queue.headMap(sequenceNumber);
    var tail = queue.tailMap(sequenceNumber);
    var state = new State(body, head);

    if (!canSwapTop(state, tail)) {
      logger.info("Moving down entry in building queue failed, cannot swap top: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new CannotMoveException();
    }

    // canSwapTop == true implies tail.size >= 2.
    assert tail.size() >= 2;
    var it = tail.entrySet().iterator();
    var entry = it.next().getValue();
    var n = it.next();
    var next = n.getValue();
    var nextSeq = n.getKey();

    if (!head.isEmpty()) {
      // The entry is not the first in the queue, just swap it with the next.
      logger.info("Moving down entry in building queue successful, the entry isn't the first: bodyId={}" +
              " sequenceNumber={}",
          bodyId, sequenceNumber);
    } else {
      // The first entry.

      var firstKind = entry.kind();
      var firstAction = entry.action();
      assert firstAction == BuildingQueueAction.CONSTRUCT || firstAction == BuildingQueueAction.DESTROY;
      var firstLevel = state.buildings.getOrDefault(firstKind, 0) +
          (firstAction == BuildingQueueAction.CONSTRUCT ? 1 : -1);
      assert firstLevel >= 0;
      var firstCost = ItemCostUtils.getCost(firstKind, firstLevel);

      var secondKind = next.kind();
      var secondAction = next.action();
      assert secondAction == BuildingQueueAction.CONSTRUCT || secondAction == BuildingQueueAction.DESTROY;
      var secondLevel = state.buildings.getOrDefault(secondKind, 0) +
          (secondAction == BuildingQueueAction.CONSTRUCT ? 1 : -1);
      assert secondLevel >= 0;
      var secondCost = ItemCostUtils.getCost(secondKind, secondLevel);

      body.getResources().add(firstCost);
      if (!body.getResources().greaterOrEqual(secondCost)) {
        logger.info("Moving down entry in building queue failed, not enough resources: bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        throw new NotEnoughResourcesException();
      }
      body.getResources().sub(secondCost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(secondKind, secondLevel);
      if (requiredEnergy > 0) {
        var totalEnergy = bodyServiceInternal.getProduction(body).totalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.info("Moving down entry in building queue failed, not enough energy: bodyId={} sequenceNumber={}",
              bodyId, sequenceNumber);
          throw new NotEnoughEnergyException();
        }
      }

      var secondItem = Item.get(secondKind);
      if (!ItemRequirementsUtils.meetsTechnologiesRequirements(secondItem, body.getUser())) {
        logger.info("Moving down entry in building queue failed, requirements not met: bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        throw new RequirementsNotMetException();
      }

      var eventOptional = eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, bodyId);
      if (eventOptional.isEmpty()) {
        logger.error("Moving down entry in building queue failed, the event is not present: bodyId={}" +
                " sequenceNumber={}",
            bodyId, sequenceNumber);
        throw new MissingEventException();
      }
      var event = eventOptional.get();

      var requiredTime = secondAction == BuildingQueueAction.CONSTRUCT ?
          getConstructionTime(secondCost, state.buildings) : getDestructionTime(secondCost, state.buildings);

      logger.info("Moving down entry in building queue successful, the entry is the first, adding an event for the" +
              " next entry: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      var now = body.getUpdatedAt();
      var at = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      event.setAt(at);
      eventScheduler.schedule(event);
    }

    // Swap.
    queue.put(sequenceNumber, next);
    queue.put(nextSeq, entry);

    body.setBuildingQueue(queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void moveUp(long bodyId, int sequenceNumber) {
    var body = bodyRepository.getOne(bodyId);
    var queue = body.getBuildingQueue();

    if (!queue.containsKey(sequenceNumber)) {
      logger.info("Moving up entry in building queue failed, no such queue entry: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    var head = queue.headMap(sequenceNumber);
    if (head.isEmpty()) {
      logger.info("Moving up entry in building queue failed, the entry is first: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new CannotMoveException();
    }

    // Moving an entry up is equivalent to moving the previous one down.
    moveDown(bodyId, head.lastKey());
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void cancel(long bodyId, int sequenceNumber) {
    var body = bodyServiceInternal.getUpdated(bodyId);
    var queue = body.getBuildingQueue();

    if (!queue.containsKey(sequenceNumber)) {
      logger.info("Cancelling entry in building queue failed, no such queue entry: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    var head = queue.headMap(sequenceNumber);
    var tail = queue.tailMap(sequenceNumber);
    var state = new State(body, head);

    if (!canRemoveTop(state, tail)) {
      logger.info("Cancelling entry in building queue failed, cannot remove top: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new CannotCancelException();
    }

    if (!head.isEmpty()) {
      // The entry is not the first in the queue, just remove it.
      logger.info("Cancelling entry in building queue successful, the entry isn't the first: bodyId={}" +
              " sequenceNumber={}",
          bodyId, sequenceNumber);
      queue.remove(sequenceNumber);
    } else {
      // The first entry.

      var it = tail.values().iterator();
      var entry = it.next();
      var kind = entry.kind();
      var action = entry.action();

      it.remove();

      assert action == BuildingQueueAction.CONSTRUCT || action == BuildingQueueAction.DESTROY;
      var level = state.buildings.getOrDefault(kind, 0) + (action == BuildingQueueAction.CONSTRUCT ? 1 : -1);
      assert level >= 0;

      var cost = ItemCostUtils.getCost(kind, level);
      body.getResources().add(cost);

      var eventOptional = eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, bodyId);
      if (eventOptional.isEmpty()) {
        logger.error("Cancelling entry in building queue failed, the event is not present: bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        throw new MissingEventException();
      }
      var event = eventOptional.get();

      if (!it.hasNext()) {
        logger.info("Cancelling entry in building queue successful, queue is empty now: bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        assert queue.isEmpty();
        eventRepository.delete(event);
      } else {
        // Get the next item.
        var next = it.next();
        kind = next.kind();
        action = next.action();

        assert action == BuildingQueueAction.CONSTRUCT || action == BuildingQueueAction.DESTROY;
        level = state.buildings.getOrDefault(kind, 0) + (action == BuildingQueueAction.CONSTRUCT ? 1 : -1);
        assert level >= 0;

        cost = ItemCostUtils.getCost(kind, level);
        if (!body.getResources().greaterOrEqual(cost)) {
          logger.info("Cancelling entry in building queue failed, not enough resources: bodyId={} sequenceNumber={}",
              bodyId, sequenceNumber);
          throw new NotEnoughResourcesException();
        }
        body.getResources().sub(cost);

        var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, level);
        if (requiredEnergy > 0) {
          var totalEnergy = bodyServiceInternal.getProduction(body).totalEnergy();
          if (requiredEnergy > totalEnergy) {
            logger.info("Cancelling entry in building queue failed, not enough energy: bodyId={} sequenceNumber={}",
                bodyId, sequenceNumber);
            throw new NotEnoughEnergyException();
          }
        }

        var item = Item.get(kind);
        if (!ItemRequirementsUtils.meetsTechnologiesRequirements(item, body.getUser())) {
          logger.info("Cancelling entry in building queue failed, requirements not met: bodyId={} sequenceNumber={}",
              bodyId, sequenceNumber);
          throw new RequirementsNotMetException();
        }

        var requiredTime = action == BuildingQueueAction.CONSTRUCT ? getConstructionTime(cost, state.buildings) :
            getDestructionTime(cost, state.buildings);

        logger.info("Cancelling entry in building queue successful, the entry is the first, modifying the event:" +
                " bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        var now = body.getUpdatedAt();
        var at = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
        event.setAt(at);
        eventScheduler.schedule(event);
      }
    }

    body.setBuildingQueue(queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void handle(Event event) {
    assert event.getKind() == EventKind.BUILDING_QUEUE;

    var bodyId = event.getParam();
    var body = bodyRepository.getById(bodyId);

    eventRepository.delete(event);

    var queue = body.getBuildingQueue();
    var it = queue.entrySet().iterator();

    // This shouldn't happen.
    if (!it.hasNext()) {
      logger.error("Handling building queue, queue is empty: bodyId={}", bodyId);
      return;
    }

    var n = it.next();
    var seq = n.getKey();
    var entry = n.getValue();

    it.remove();

    bodyServiceInternal.updateResourcesAndShipyard(body, event.getAt());

    // Update buildings.
    var oldLevel = body.getBuildingLevel(entry.kind());
    assert oldLevel >= 0;
    var newLevel = oldLevel + (entry.action() == BuildingQueueAction.CONSTRUCT ? 1 : -1);
    assert newLevel >= 0;
    logger.info("Handling building queue, updating building level: bodyId={} kind={} oldLevel={} newLevel={}",
        bodyId, entry.kind(), oldLevel, newLevel);
    body.setBuildingLevel(entry.kind(), newLevel);

    // Handle subsequent entries.

    var totalEnergy = bodyServiceInternal.getProduction(body).totalEnergy();
    var usedFields = bodyServiceInternal.getUsedFields(body);
    var maxFields = bodyServiceInternal.getMaxFields(body);

    while (it.hasNext()) {
      n = it.next();
      seq = n.getKey();
      entry = n.getValue();

      var action = entry.action();
      var level = body.getBuildingLevel(entry.kind());
      assert level >= 0;

      if (action == BuildingQueueAction.CONSTRUCT && usedFields >= maxFields) {
        logger.info("Handling building queue, removing entry, no more free fields: bodyId={} kind={} sequenceNumber={}",
            bodyId, entry.kind(), seq);
        it.remove();
        continue;
      }

      if (action == BuildingQueueAction.DESTROY && level == 0) {
        logger.error("Handling building queue, destroying non-existing building: bodyId={} kind={} sequenceNumber={}",
            bodyId, entry.kind(), seq);
        it.remove();
        continue;
      }

      assert action == BuildingQueueAction.CONSTRUCT || action == BuildingQueueAction.DESTROY;
      level += action == BuildingQueueAction.CONSTRUCT ? 1 : -1;

      var cost = ItemCostUtils.getCost(entry.kind(), level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.info("Handling building queue, removing entry, not enough resources: bodyId={} kind={}" +
                " sequenceNumber={}",
            bodyId, entry.kind(), seq);
        it.remove();
        continue;
      }

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(entry.kind(), level);
      if (requiredEnergy > totalEnergy) {
        logger.info("Handling building queue, removing entry, not enough energy: bodyId={} kind={}" +
                " sequenceNumber={}",
            bodyId, entry.kind(), seq);
        it.remove();
        continue;
      }

      var item = Item.get(entry.kind());
      if (!ItemRequirementsUtils.meetsRequirements(item, body)) {
        logger.info("Handling building queue, removing entry, requirements not met: bodyId={} kind={}" +
                " sequenceNumber={}",
            bodyId, entry.kind(), seq);
        it.remove();
        continue;
      }

      logger.info("Handling building queue, creating an event: bodyId={} kind={} action={} level={} sequenceNumber={}",
          bodyId, entry.kind(), action, level, seq);
      body.getResources().sub(cost);
      long requiredTime = action == BuildingQueueAction.CONSTRUCT ? getConstructionTime(cost, body) :
          getDestructionTime(cost, body);
      Date startAt = Date.from(Instant.ofEpochSecond(event.getAt().toInstant().getEpochSecond() + requiredTime));
      Event newEvent = new Event();
      newEvent.setAt(startAt);
      newEvent.setKind(EventKind.BUILDING_QUEUE);
      newEvent.setParam(bodyId);
      eventScheduler.schedule(newEvent);

      break;
    }

    body.setBuildingQueue(queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void deleteBuildingsAndQueue(Body body) {
    Optional<Event> event = eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, body.getId());
    event.ifPresent(eventRepository::delete);
  }

  private long getConstructionTime(Resources cost, Body body) {
    var roboticsFactoryLevel = body.getBuildingLevel(BuildingKind.ROBOTICS_FACTORY);
    var naniteFactoryLevel = body.getBuildingLevel(BuildingKind.NANITE_FACTORY);
    return itemTimeUtils.getBuildingConstructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);
  }

  private long getConstructionTime(Resources cost, Map<BuildingKind, Integer> buildings) {
    var roboticsFactoryLevel = buildings.getOrDefault(BuildingKind.ROBOTICS_FACTORY, 0);
    var naniteFactoryLevel = buildings.getOrDefault(BuildingKind.NANITE_FACTORY, 0);
    return itemTimeUtils.getBuildingConstructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);
  }

  private long getDestructionTime(Resources cost, Body body) {
    var roboticsFactoryLevel = body.getBuildingLevel(BuildingKind.ROBOTICS_FACTORY);
    var naniteFactoryLevel = body.getBuildingLevel(BuildingKind.NANITE_FACTORY);
    return itemTimeUtils.getBuildingDestructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);
  }

  private long getDestructionTime(Resources cost, Map<BuildingKind, Integer> buildings) {
    var roboticsFactoryLevel = buildings.getOrDefault(BuildingKind.ROBOTICS_FACTORY, 0);
    var naniteFactoryLevel = buildings.getOrDefault(BuildingKind.NANITE_FACTORY, 0);
    return itemTimeUtils.getBuildingDestructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);
  }

  // Checks whether it is possible to swap top two items in the queue ignoring resources.
  private boolean canSwapTop(State state, SortedMap<Integer, BuildingQueueEntry> queue) {
    if (queue.size() < 2) {
      return false;
    }

    // Get first two items.
    var it = queue.values().iterator();
    var first = it.next();
    var second = it.next();

    // Check requirements.
    // The second building will always meet requirements when the first action is destroy.
    if (first.action() == BuildingQueueAction.CONSTRUCT) {
      if (second.action() == BuildingQueueAction.CONSTRUCT) {
        var requirements = Item.get(second.kind()).getBuildingsRequirements();
        if (requirements.getOrDefault(first.kind(), 0) >
            state.buildings.getOrDefault(first.kind(), 0)) {
          return false;
        }
      } else {
        assert second.action() == BuildingQueueAction.DESTROY;
        if (!state.buildings.containsKey(second.kind())) {
          return false;
        }
        var requirements = Item.get(first.kind()).getBuildingsRequirements();
        int levelAfterDeconstruction = state.buildings.get(second.kind()) - 1;
        assert levelAfterDeconstruction >= 0;
        if (requirements.getOrDefault(second.kind(), 0) > levelAfterDeconstruction) {
          return false;
        }
      }
    }

    // Check body's fields.
    // If the second action is destroy, there will be always enough free fields after the swap.
    if (second.action() == BuildingQueueAction.CONSTRUCT) {
      if (first.action() == BuildingQueueAction.DESTROY) {
        // The destruction can free one field and thus we can construct the second one, but after the swap there may not
        // be enough fields to construct it.
        if (state.usedFields >= state.maxFields) {
          return false;
        }
      } else if ((first.kind() == BuildingKind.TERRAFORMER ||
          first.kind() == BuildingKind.LUNAR_BASE) && second.kind() != BuildingKind.TERRAFORMER &&
          second.kind() != BuildingKind.LUNAR_BASE && state.usedFields + 1 >= state.maxFields) {
        // After the second one would be built, there won't be free fields anymore, as the second one doesn't increase
        // the max fields like the first one.
        return false;
      }
    }

    return true;
  }

  private boolean canRemoveTop(State state, SortedMap<Integer, BuildingQueueEntry> queue) {
    if (queue.isEmpty()) {
      return false;
    }

    var it = queue.values().iterator();
    var firstKind = it.next().kind();

    var usedFields = state.usedFields;
    var maxFields = state.maxFields;
    var level = state.buildings.getOrDefault(firstKind, 0);

    while (it.hasNext()) {
      // Check whether there is enough fields to construct the building. This must be checked, because we may remove
      // construction of a terraformer or lunar base, or remove destruction of a building.
      if (usedFields >= maxFields) {
        return false;
      }
      usedFields++;

      var current = it.next();
      var currentAction = current.action();
      var currentKind = current.kind();

      // Increase the max fields.
      // Terraformer and lunar base cannot be destroyed once built.
      assert !(currentKind == BuildingKind.TERRAFORMER || currentKind == BuildingKind.LUNAR_BASE) ||
          currentAction == BuildingQueueAction.CONSTRUCT;
      if (currentKind == BuildingKind.TERRAFORMER) {
        maxFields += fieldsPerTerraformerLevel;
      } else if (currentKind == BuildingKind.LUNAR_BASE) {
        maxFields += fieldsPerLunarBaseLevel;
      }

      if (currentKind == firstKind) {
        if (currentAction == BuildingQueueAction.CONSTRUCT) {
          level++;
        } else {
          assert currentAction == BuildingQueueAction.DESTROY;
          if (level == 0) {
            return false;
          }
          level--;
        }
      } else {
        var requirements = Item.get(currentKind).getBuildingsRequirements();
        if (requirements.getOrDefault(firstKind, 0) > level) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean meetsSpecialRequirements(Body body, BuildingItem item, BuildingKind kind) {
    boolean meetsRequirements = item.meetsSpecialRequirements(body);
    if (kind == BuildingKind.NANITE_FACTORY && body.getCoordinates().getKind() == CoordinatesKind.MOON &&
        !allowNanitesOnMoon) {
      meetsRequirements = false;
    }

    return meetsRequirements;
  }
}
