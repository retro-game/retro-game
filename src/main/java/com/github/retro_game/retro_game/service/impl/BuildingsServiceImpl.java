package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.Item;
import com.github.retro_game.retro_game.model.ItemCostUtils;
import com.github.retro_game.retro_game.model.ItemRequirementsUtils;
import com.github.retro_game.retro_game.model.ItemTimeUtils;
import com.github.retro_game.retro_game.model.building.BuildingItem;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.repository.BuildingQueueEntryRepository;
import com.github.retro_game.retro_game.repository.BuildingRepository;
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
class BuildingsServiceImpl implements BuildingsServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(BuildingsServiceImpl.class);
  private final int buildingQueueCapacity;
  private final int fieldsPerTerraformerLevel;
  private final int fieldsPerLunarBaseLevel;
  private final ItemTimeUtils itemTimeUtils;
  private final BodyRepository bodyRepository;
  private final BuildingQueueEntryRepository buildingQueueEntryRepository;
  private final BuildingRepository buildingRepository;
  private final EventRepository eventRepository;
  private BodyServiceInternal bodyServiceInternal;
  private EventScheduler eventScheduler;

  private class State {
    final Map<BuildingKind, Integer> buildings;
    int usedFields;
    int maxFields;

    State(Body body, SortedMap<Integer, BuildingQueueEntry> queue) {
      buildings = new EnumMap<>(BuildingKind.class);
      for (Map.Entry<BuildingKind, Building> entry : body.getBuildings().entrySet()) {
        int level = entry.getValue().getLevel();
        buildings.put(entry.getKey(), level);
        usedFields += level;
      }
      maxFields = bodyServiceInternal.getMaxFields(body, buildings);
      if (queue != null) {
        for (BuildingQueueEntry entry : queue.values()) {
          if (entry.getAction() == BuildingQueueAction.CONSTRUCT) {
            construct(entry.getKind());
          } else {
            assert entry.getAction() == BuildingQueueAction.DESTROY;
            destroy(entry.getKind());
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
                              ItemTimeUtils itemTimeUtils, BodyRepository bodyRepository,
                              BuildingQueueEntryRepository buildingQueueEntryRepository,
                              BuildingRepository buildingRepository, EventRepository eventRepository) {
    this.buildingQueueCapacity = buildingQueueCapacity;
    this.fieldsPerTerraformerLevel = fieldsPerTerraformerLevel;
    this.fieldsPerLunarBaseLevel = fieldsPerLunarBaseLevel;
    this.itemTimeUtils = itemTimeUtils;
    this.bodyRepository = bodyRepository;
    this.buildingQueueEntryRepository = buildingQueueEntryRepository;
    this.buildingRepository = buildingRepository;
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
    Body body = bodyServiceInternal.getUpdated(bodyId);
    User user = body.getUser();
    Resources resources = body.getResources();
    final int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();

    State state = new State(body, null);

    SortedMap<Integer, BuildingQueueEntry> buildingQueue = body.getBuildingQueue();
    int size = buildingQueue.size();
    List<BuildingQueueEntryDto> queue = new ArrayList<>(size);
    if (size > 0) {
      Iterator<Map.Entry<Integer, BuildingQueueEntry>> it = buildingQueue.entrySet().iterator();
      Map.Entry<Integer, BuildingQueueEntry> next = it.next();
      boolean first = true;
      long finishAt = 0;
      boolean upMovable = false;
      do {
        Map.Entry<Integer, BuildingQueueEntry> entry = next;
        next = it.hasNext() ? it.next() : null;

        BuildingQueueEntry queueEntry = entry.getValue();
        BuildingKind kind = queueEntry.getKind();
        BuildingQueueAction action = queueEntry.getAction();

        int levelFrom = state.buildings.getOrDefault(kind, 0);
        assert action == BuildingQueueAction.CONSTRUCT || action == BuildingQueueAction.DESTROY;
        int levelTo = levelFrom + (action == BuildingQueueAction.CONSTRUCT ? 1 : -1);
        assert levelTo >= 0;

        var cost = ItemCostUtils.getCost(kind, levelTo);
        var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, levelTo);

        long requiredTime;
        if (first) {
          Optional<Event> event = eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, bodyId);
          Assert.isTrue(event.isPresent(), "Event must be present");
          finishAt = event.get().getAt().toInstant().getEpochSecond();
          long now = body.getUpdatedAt().toInstant().getEpochSecond();
          requiredTime = finishAt - now;
        } else {
          if (action == BuildingQueueAction.CONSTRUCT) {
            requiredTime = getConstructionTime(cost, state.buildings);
          } else {
            requiredTime = getDestructionTime(cost, state.buildings);
          }
          finishAt += requiredTime;
        }

        // Check dependencies of subsequent entries.
        SortedMap<Integer, BuildingQueueEntry> tail = buildingQueue.tailMap(entry.getKey());
        boolean downMovable = canSwapTop(state, tail);
        boolean cancelable = canRemoveTop(state, tail);

        // Moving down or cancelling the first entry is equivalent to building the second one, which is the reason
        // for checking resources.
        if (first && next != null) {
          BuildingQueueAction nextAction = next.getValue().getAction();
          BuildingKind nextKind = next.getValue().getKind();

          assert nextAction == BuildingQueueAction.CONSTRUCT || nextAction == BuildingQueueAction.DESTROY;
          int nextLevel = state.buildings.getOrDefault(nextKind, 0) +
              (nextAction == BuildingQueueAction.CONSTRUCT ? 1 : -1);
          assert nextLevel >= 0;

          var nextCost = ItemCostUtils.getCost(nextKind, nextLevel);
          nextCost.sub(cost);
          if (!resources.greaterOrEqual(nextCost)) {
            downMovable = cancelable = false;
          }

          var nextRequiredEnergy = ItemCostUtils.getRequiredEnergy(nextKind, nextLevel);
          if (nextRequiredEnergy > totalEnergy) {
            downMovable = cancelable = false;
          }

          var nextItem = Item.get(nextKind);
          if (!ItemRequirementsUtils.meetsTechnologiesRequirements(nextItem, user)) {
            downMovable = cancelable = false;
          }
        }

        queue.add(new BuildingQueueEntryDto(Converter.convert(kind), entry.getKey(), levelFrom, levelTo,
            Converter.convert(cost), requiredEnergy, Date.from(Instant.ofEpochSecond(finishAt)), requiredTime,
            downMovable, upMovable, cancelable));

        if (action == BuildingQueueAction.CONSTRUCT) {
          state.construct(kind);
        } else {
          state.destroy(kind);
        }

        first = false;
        upMovable = downMovable;
      } while (next != null);
    }

    boolean canConstruct = state.usedFields < state.maxFields && queue.size() < buildingQueueCapacity;
    List<BuildingDto> buildings = new ArrayList<>();
    for (Map.Entry<BuildingKind, BuildingItem> entry : BuildingItem.getAll().entrySet()) {
      BuildingKind kind = entry.getKey();
      BuildingItem item = entry.getValue();
      boolean meetsRequirements = item.meetsSpecialRequirements(body) &&
          ItemRequirementsUtils.meetsBuildingsRequirements(item, state.buildings) && (!queue.isEmpty() ||
          ItemRequirementsUtils.meetsTechnologiesRequirements(item, user));
      if (meetsRequirements || state.buildings.containsKey(kind)) {
        Building building = body.getBuildings().get(kind);
        int currentLevel = building == null ? 0 : building.getLevel();
        int futureLevel = state.buildings.getOrDefault(kind, 0);

        var cost = ItemCostUtils.getCost(kind, futureLevel + 1);
        var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, futureLevel + 1);
        long constructionTime = getConstructionTime(cost, state.buildings);
        boolean canConstructNow = canConstruct && meetsRequirements &&
            (!queue.isEmpty() || (resources.greaterOrEqual(cost) && totalEnergy >= requiredEnergy));

        buildings.add(new BuildingDto(Converter.convert(kind), currentLevel, futureLevel, Converter.convert(cost),
            requiredEnergy, constructionTime, canConstructNow));
      }
    }
    // Keep the order defined in the service layer.
    buildings.sort(Comparator.comparing(BuildingDto::getKind));

    return new BuildingsAndQueuePairDto(buildings, queue);
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
    SortedMap<Integer, BuildingQueueEntry> buildingQueue = body.getBuildingQueue();
    if (buildingQueue.isEmpty()) {
      return Optional.empty();
    }
    BuildingQueueEntry first = buildingQueue.get(buildingQueue.firstKey());
    assert first != null;
    BuildingKind kind = first.getKind();
    BuildingQueueAction action = first.getAction();
    Building building = body.getBuildings().get(kind);
    int level = (building != null ? building.getLevel() : 0) + (action == BuildingQueueAction.CONSTRUCT ? 1 : -1);
    return Optional.of(new OngoingBuildingDto(kind, level));
  }

  @Override
  public Optional<Date> getOngoingBuildingFinishAt(Body body) {
    return eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, body.getId()).map(Event::getAt);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void construct(long bodyId, BuildingKindDto kind) {
    BuildingKind k = Converter.convert(kind);

    Body body = bodyServiceInternal.getUpdated(bodyId);

    SortedMap<Integer, BuildingQueueEntry> queue = body.getBuildingQueue();
    if (queue.size() >= buildingQueueCapacity) {
      logger.warn("Constructing building failed, queue is full: bodyId={} kind={}", bodyId, k);
      throw new QueueFullException();
    }

    State state = new State(body, queue);
    if (state.usedFields >= state.maxFields) {
      logger.warn("Constructing building failed, no more free fields: bodyId={} kind={}", bodyId, k);
      throw new NoMoreFreeFieldsException();
    }

    var item = Item.get(k);
    if (!item.meetsSpecialRequirements(body) || !ItemRequirementsUtils.meetsBuildingsRequirements(item, state.buildings) ||
        (queue.isEmpty() && !ItemRequirementsUtils.meetsTechnologiesRequirements(item, body.getUser()))) {
      logger.warn("Constructing building failed, requirements not met: bodyId={} kind={}", bodyId, k);
      throw new RequirementsNotMetException();
    }

    BuildingQueueEntryKey key = new BuildingQueueEntryKey();
    key.setBody(body);
    if (!queue.isEmpty()) {
      int sequenceNumber = queue.lastKey() + 1;
      key.setSequence(sequenceNumber);
      logger.info("Constructing building successful, appending to queue: bodyId={} kind={} sequenceNumber={}",
          bodyId, k, sequenceNumber);
    } else {
      int level = state.buildings.getOrDefault(k, 0) + 1;

      var cost = ItemCostUtils.getCost(k, level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.warn("Constructing building failed, not enough resources: bodyId={} kind={}", bodyId, k);
        throw new NotEnoughResourcesException();
      }
      body.getResources().sub(cost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(k, level);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.warn("Constructing building failed, not enough energy: bodyId={} kind={}", bodyId, k);
          throw new NotEnoughEnergyException();
        }
      }

      logger.info("Constructing building successful, creating a new event: bodyId={} kind={}", bodyId, k);
      Date now = body.getUpdatedAt();
      long requiredTime = getConstructionTime(cost, state.buildings);
      Date startAt = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      Event event = new Event();
      event.setAt(startAt);
      event.setKind(EventKind.BUILDING_QUEUE);
      event.setParam(bodyId);
      eventScheduler.schedule(event);

      key.setSequence(1);
    }

    BuildingQueueEntry entry = new BuildingQueueEntry();
    entry.setKey(key);
    entry.setKind(k);
    entry.setAction(BuildingQueueAction.CONSTRUCT);
    buildingQueueEntryRepository.save(entry);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void destroy(long bodyId, BuildingKindDto kind) {
    BuildingKind k = Converter.convert(kind);

    Body body = bodyServiceInternal.getUpdated(bodyId);

    if (k == BuildingKind.TERRAFORMER || k == BuildingKind.LUNAR_BASE) {
      logger.warn("Destroying building failed, cannot destroy this building: bodyId={} kind={}", bodyId, k);
      throw new WrongBuildingKindException();
    }

    SortedMap<Integer, BuildingQueueEntry> queue = body.getBuildingQueue();
    if (queue.size() >= buildingQueueCapacity) {
      logger.warn("Destroying building failed, queue is full: bodyId={} kind={}", bodyId, k);
      throw new QueueFullException();
    }

    State state = new State(body, queue);
    if (state.buildings.getOrDefault(k, 0) == 0) {
      logger.warn("Destroying building failed, the building is already going to be fully destroyed: bodyId={} kind={}",
          bodyId, k);
      throw new BuildingAlreadyDestroyedException();
    }

    BuildingQueueEntryKey key = new BuildingQueueEntryKey();
    key.setBody(body);

    if (!queue.isEmpty()) {
      int sequenceNumber = queue.lastKey() + 1;
      key.setSequence(sequenceNumber);
      logger.info("Destroying building successful, appending to queue: bodyId={} kind={} sequenceNumber={}",
          bodyId, k, sequenceNumber);
    } else {
      assert state.buildings.containsKey(k);
      int level = state.buildings.get(k) - 1;
      assert level >= 0;

      var cost = ItemCostUtils.getCost(k, level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.warn("Destroying building failed, not enough resources: bodyId={} kind={}", bodyId, k);
        throw new NotEnoughResourcesException();
      }
      body.getResources().sub(cost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(k, level);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.warn("Destroying building failed, not enough energy: bodyId={} kind={}", bodyId, k);
          throw new NotEnoughEnergyException();
        }
      }

      logger.info("Destroying building successful, create a new event: bodyId={} kind={}", bodyId, k);
      Date now = body.getUpdatedAt();
      long requiredTime = getDestructionTime(cost, state.buildings);
      Date startAt = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      Event event = new Event();
      event.setAt(startAt);
      event.setKind(EventKind.BUILDING_QUEUE);
      event.setParam(bodyId);
      eventScheduler.schedule(event);

      key.setSequence(1);
    }

    BuildingQueueEntry entry = new BuildingQueueEntry();
    entry.setKey(key);
    entry.setKind(k);
    entry.setAction(BuildingQueueAction.DESTROY);
    buildingQueueEntryRepository.save(entry);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void moveDown(long bodyId, int sequenceNumber) {
    Body body = bodyServiceInternal.getUpdated(bodyId);

    SortedMap<Integer, BuildingQueueEntry> queue = body.getBuildingQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.warn("Moving down entry in building queue failed, no such queue entry: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    SortedMap<Integer, BuildingQueueEntry> head = queue.headMap(sequenceNumber);
    SortedMap<Integer, BuildingQueueEntry> tail = queue.tailMap(sequenceNumber);
    State state = new State(body, head);

    if (!canSwapTop(state, tail)) {
      logger.warn("Moving down entry in building queue failed, cannot swap top: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new CannotMoveException();
    }

    // canSwapTop == true implies tail.size >= 2.
    assert tail.size() >= 2;
    Iterator<BuildingQueueEntry> it = tail.values().iterator();
    BuildingQueueEntry entry = it.next();
    BuildingQueueEntry next = it.next();

    if (!head.isEmpty()) {
      // The entry is not the first in the queue, just swap it with the next.
      logger.info("Moving down entry in building queue successful, the entry isn't the first: bodyId={}" +
              " sequenceNumber={}",
          bodyId, sequenceNumber);
    } else {
      // The first entry.

      BuildingKind firstKind = entry.getKind();
      BuildingQueueAction firstAction = entry.getAction();
      assert firstAction == BuildingQueueAction.CONSTRUCT || firstAction == BuildingQueueAction.DESTROY;
      int firstLevel = state.buildings.getOrDefault(firstKind, 0) +
          (firstAction == BuildingQueueAction.CONSTRUCT ? 1 : -1);
      assert firstLevel >= 0;
      var firstCost = ItemCostUtils.getCost(firstKind, firstLevel);

      BuildingKind secondKind = next.getKind();
      BuildingQueueAction secondAction = next.getAction();
      assert secondAction == BuildingQueueAction.CONSTRUCT || secondAction == BuildingQueueAction.DESTROY;
      int secondLevel = state.buildings.getOrDefault(secondKind, 0) +
          (secondAction == BuildingQueueAction.CONSTRUCT ? 1 : -1);
      assert secondLevel >= 0;
      var secondCost = ItemCostUtils.getCost(secondKind, secondLevel);

      body.getResources().add(firstCost);
      if (!body.getResources().greaterOrEqual(secondCost)) {
        logger.warn("Moving down entry in building queue failed, not enough resources: bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        throw new NotEnoughResourcesException();
      }
      body.getResources().sub(secondCost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(secondKind, secondLevel);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.warn("Moving down entry in building queue failed, not enough energy: bodyId={} sequenceNumber={}",
              bodyId, sequenceNumber);
          throw new NotEnoughEnergyException();
        }
      }

      var secondItem = Item.get(secondKind);
      if (!ItemRequirementsUtils.meetsTechnologiesRequirements(secondItem, body.getUser())) {
        logger.warn("Moving down entry in building queue failed, requirements not met: bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        throw new RequirementsNotMetException();
      }

      Optional<Event> eventOptional = eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, bodyId);
      if (!eventOptional.isPresent()) {
        logger.error("Moving down entry in building queue failed, the event is not present: bodyId={}" +
                " sequenceNumber={}",
            bodyId, sequenceNumber);
        throw new MissingEventException();
      }
      Event event = eventOptional.get();

      long requiredTime = secondAction == BuildingQueueAction.CONSTRUCT ?
          getConstructionTime(secondCost, state.buildings) : getDestructionTime(secondCost, state.buildings);

      logger.info("Moving down entry in building queue successful, the entry is the first, adding an event for the" +
              " next entry: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      Date now = body.getUpdatedAt();
      Date at = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      event.setAt(at);
      eventScheduler.schedule(event);
    }

    // Swap.
    BuildingKind kind = entry.getKind();
    BuildingQueueAction action = entry.getAction();
    entry.setKind(next.getKind());
    entry.setAction(next.getAction());
    next.setKind(kind);
    next.setAction(action);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void moveUp(long bodyId, int sequenceNumber) {
    Body body = bodyRepository.getOne(bodyId);

    SortedMap<Integer, BuildingQueueEntry> queue = body.getBuildingQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.warn("Moving up entry in building queue failed, no such queue entry: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    SortedMap<Integer, BuildingQueueEntry> head = queue.headMap(sequenceNumber);
    if (head.isEmpty()) {
      logger.warn("Moving up entry in building queue failed, the entry is first: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new CannotMoveException();
    }

    // Moving an entry up is equivalent to moving the previous one down.
    moveDown(bodyId, head.lastKey());
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void cancel(long bodyId, int sequenceNumber) {
    Body body = bodyServiceInternal.getUpdated(bodyId);

    SortedMap<Integer, BuildingQueueEntry> queue = body.getBuildingQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.warn("Cancelling entry in building queue failed, no such queue entry: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    SortedMap<Integer, BuildingQueueEntry> head = queue.headMap(sequenceNumber);
    SortedMap<Integer, BuildingQueueEntry> tail = queue.tailMap(sequenceNumber);
    State state = new State(body, head);

    if (!canRemoveTop(state, tail)) {
      logger.warn("Cancelling entry in building queue failed, cannot remove top: bodyId={} sequenceNumber={}",
          bodyId, sequenceNumber);
      throw new CannotCancelException();
    }

    if (!head.isEmpty()) {
      // The entry is not the first in the queue, just remove it.
      logger.info("Cancelling entry in building queue successful, the entry isn't the first: bodyId={}" +
              " sequenceNumber={}",
          bodyId, sequenceNumber);
      BuildingQueueEntry entry = queue.remove(sequenceNumber);
      buildingQueueEntryRepository.delete(entry);
    } else {
      // The first entry.

      Iterator<BuildingQueueEntry> it = tail.values().iterator();
      BuildingQueueEntry entry = it.next();
      BuildingKind kind = entry.getKind();
      BuildingQueueAction action = entry.getAction();

      assert action == BuildingQueueAction.CONSTRUCT || action == BuildingQueueAction.DESTROY;
      int level = state.buildings.getOrDefault(kind, 0) + (action == BuildingQueueAction.CONSTRUCT ? 1 : -1);
      assert level >= 0;

      var cost = ItemCostUtils.getCost(kind, level);
      body.getResources().add(cost);

      Optional<Event> eventOptional = eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, bodyId);
      if (!eventOptional.isPresent()) {
        logger.error("Cancelling entry in building queue failed, the event is not present: bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        throw new MissingEventException();
      }
      Event event = eventOptional.get();

      if (!it.hasNext()) {
        logger.info("Cancelling entry in building queue successful, queue is empty now: bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        eventRepository.delete(event);
      } else {
        // Get the next item.
        BuildingQueueEntry next = it.next();
        kind = next.getKind();
        action = next.getAction();

        assert action == BuildingQueueAction.CONSTRUCT || action == BuildingQueueAction.DESTROY;
        level = state.buildings.getOrDefault(kind, 0) + (action == BuildingQueueAction.CONSTRUCT ? 1 : -1);
        assert level >= 0;

        cost = ItemCostUtils.getCost(kind, level);
        if (!body.getResources().greaterOrEqual(cost)) {
          logger.warn("Cancelling entry in building queue failed, not enough resources: bodyId={} sequenceNumber={}",
              bodyId, sequenceNumber);
          throw new NotEnoughResourcesException();
        }
        body.getResources().sub(cost);

        var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, level);
        if (requiredEnergy > 0) {
          int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();
          if (requiredEnergy > totalEnergy) {
            logger.warn("Cancelling entry in building queue failed, not enough energy: bodyId={} sequenceNumber={}",
                bodyId, sequenceNumber);
            throw new NotEnoughEnergyException();
          }
        }

        var item = Item.get(kind);
        if (!ItemRequirementsUtils.meetsTechnologiesRequirements(item, body.getUser())) {
          logger.warn("Cancelling entry in building queue failed, requirements not met: bodyId={} sequenceNumber={}",
              bodyId, sequenceNumber);
          throw new RequirementsNotMetException();
        }

        long requiredTime = action == BuildingQueueAction.CONSTRUCT ? getConstructionTime(cost, state.buildings) :
            getDestructionTime(cost, state.buildings);

        logger.info("Cancelling entry in building queue successful, the entry is the first, modifying the event:" +
                " bodyId={} sequenceNumber={}",
            bodyId, sequenceNumber);
        Date now = body.getUpdatedAt();
        Date at = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
        event.setAt(at);
        eventScheduler.schedule(event);
      }

      it.remove();
      buildingQueueEntryRepository.delete(entry);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void handle(Event event) {
    long bodyId = event.getParam();
    Body body = bodyRepository.getOne(bodyId);

    Date at = event.getAt();

    eventRepository.delete(event);

    Collection<BuildingQueueEntry> values = body.getBuildingQueue().values();
    Iterator<BuildingQueueEntry> it = values.iterator();

    // This shouldn't happen.
    if (!it.hasNext()) {
      logger.error("Handling building queue, queue is empty: bodyId={}", bodyId);
      return;
    }

    BuildingQueueEntry entry = it.next();
    BuildingKind kind = entry.getKind();

    it.remove();
    buildingQueueEntryRepository.delete(entry);

    bodyServiceInternal.updateResources(body, at);

    // Update buildings.
    Map<BuildingKind, Building> buildings = body.getBuildings();
    Building building = buildings.get(kind);
    if (entry.getAction() == BuildingQueueAction.CONSTRUCT) {
      if (building != null) {
        int level = building.getLevel() + 1;
        logger.info("Handling building queue, increasing building level: bodyId={} kind={} level={}",
            bodyId, kind, level);
        building.setLevel(level);
      } else {
        logger.info("Handling building queue, creating building: bodyId={} kind={}", bodyId, kind);
        BuildingKey key = new BuildingKey();
        key.setBody(body);
        key.setKind(kind);
        building = new Building();
        building.setKey(key);
        building.setLevel(1);
        body.getBuildings().put(kind, building);
        buildingRepository.save(building);
      }
    } else {
      assert entry.getAction() == BuildingQueueAction.DESTROY;
      if (building == null) {
        logger.error("Handling building queue, destroying non-existing building: bodyId={} kind={}", bodyId, kind);
      } else {
        int level = building.getLevel() - 1;
        if (level >= 1) {
          logger.info("Handling building queue, decreasing building level: bodyId={} kind={} level={}",
              bodyId, kind, level);
          building.setLevel(level);
        } else {
          logger.info("Handling building queue, destroying building: bodyId={} kind={}", bodyId, kind);
          body.getBuildings().remove(kind);
          buildingRepository.delete(building);
        }
      }
    }

    // Handle subsequent entries.

    final int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();

    final int usedFields = bodyServiceInternal.getUsedFields(body);
    final int maxFields = bodyServiceInternal.getMaxFields(body);

    while (it.hasNext()) {
      entry = it.next();
      kind = entry.getKind();
      BuildingQueueAction action = entry.getAction();
      int sequenceNumber = entry.getSequence();

      if (action == BuildingQueueAction.CONSTRUCT && usedFields >= maxFields) {
        logger.info("Handling building queue, removing entry, no more free fields: bodyId={} kind={} sequenceNumber={}",
            bodyId, kind, sequenceNumber);
        it.remove();
        buildingQueueEntryRepository.delete(entry);
        continue;
      }

      building = body.getBuildings().get(kind);
      int level = building != null ? building.getLevel() : 0;
      assert level >= 0;

      if (action == BuildingQueueAction.DESTROY && level == 0) {
        logger.error("Handling building queue, destroying non-existing building: bodyId={} kind={} sequenceNumber={}",
            bodyId, kind, sequenceNumber);
        it.remove();
        buildingQueueEntryRepository.delete(entry);
        continue;
      }

      assert action == BuildingQueueAction.CONSTRUCT || action == BuildingQueueAction.DESTROY;
      level += action == BuildingQueueAction.CONSTRUCT ? 1 : -1;

      var cost = ItemCostUtils.getCost(kind, level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.info("Handling building queue, removing entry, not enough resources: bodyId={} kind={}" +
                " sequenceNumber={}",
            bodyId, kind, sequenceNumber);
        it.remove();
        buildingQueueEntryRepository.delete(entry);
        continue;
      }

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, level);
      if (requiredEnergy > totalEnergy) {
        logger.info("Handling building queue, removing entry, not enough energy: bodyId={} kind={}" +
                " sequenceNumber={}",
            bodyId, kind, sequenceNumber);
        it.remove();
        buildingQueueEntryRepository.delete(entry);
        continue;
      }

      var item = Item.get(kind);
      if (!ItemRequirementsUtils.meetsRequirements(item, body)) {
        logger.info("Handling building queue, removing entry, requirements not met: bodyId={} kind={}" +
                " sequenceNumber={}",
            bodyId, kind, sequenceNumber);
        it.remove();
        buildingQueueEntryRepository.delete(entry);
        continue;
      }

      logger.info("Handling building queue, creating an event: bodyId={} kind={} action={} level={} sequenceNumber={}",
          bodyId, kind, action, level, sequenceNumber);
      body.getResources().sub(cost);
      long requiredTime = action == BuildingQueueAction.CONSTRUCT ? getConstructionTime(cost, body) :
          getDestructionTime(cost, body);
      Date startAt = Date.from(Instant.ofEpochSecond(at.toInstant().getEpochSecond() + requiredTime));
      Event newEvent = new Event();
      newEvent.setAt(startAt);
      newEvent.setKind(EventKind.BUILDING_QUEUE);
      newEvent.setParam(bodyId);
      eventScheduler.schedule(newEvent);

      break;
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void deleteBuildingsAndQueue(Body body) {
    Optional<Event> event = eventRepository.findFirstByKindAndParam(EventKind.BUILDING_QUEUE, body.getId());
    event.ifPresent(eventRepository::delete);
    buildingQueueEntryRepository.deleteAll(body.getBuildingQueue().values());
    buildingRepository.deleteAll(body.getBuildings().values());
  }

  private long getConstructionTime(Resources cost, Body body) {
    var roboticsFactoryLevel = body.getBuildingLevel(BuildingKind.ROBOTICS_FACTORY);
    var naniteFactoryLevel = body.getBuildingLevel(BuildingKind.NANITE_FACTORY);
    return itemTimeUtils.getBuildingConstructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);
  }

  private long getConstructionTime(Resources cost, Map<BuildingKind, Integer> buildings) {
    var roboticsFactoryLevel = buildings.get(BuildingKind.ROBOTICS_FACTORY);
    var naniteFactoryLevel = buildings.get(BuildingKind.NANITE_FACTORY);
    return itemTimeUtils.getBuildingConstructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);
  }

  private long getDestructionTime(Resources cost, Body body) {
    var roboticsFactoryLevel = body.getBuildingLevel(BuildingKind.ROBOTICS_FACTORY);
    var naniteFactoryLevel = body.getBuildingLevel(BuildingKind.NANITE_FACTORY);
    return itemTimeUtils.getBuildingDestructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);
  }

  private long getDestructionTime(Resources cost, Map<BuildingKind, Integer> buildings) {
    var roboticsFactoryLevel = buildings.get(BuildingKind.ROBOTICS_FACTORY);
    var naniteFactoryLevel = buildings.get(BuildingKind.NANITE_FACTORY);
    return itemTimeUtils.getBuildingDestructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);
  }

  // Checks whether it is possible to swap top two items in the queue ignoring resources.
  private boolean canSwapTop(State state, SortedMap<Integer, BuildingQueueEntry> queue) {
    if (queue.size() < 2) {
      return false;
    }

    // Get first two items.
    Iterator<BuildingQueueEntry> it = queue.values().iterator();
    BuildingQueueEntry first = it.next();
    BuildingQueueEntry second = it.next();

    // Check requirements.
    // The second building will always meet requirements when the first action is destroy.
    if (first.getAction() == BuildingQueueAction.CONSTRUCT) {
      if (second.getAction() == BuildingQueueAction.CONSTRUCT) {
        var requirements = Item.get(second.getKind()).getBuildingsRequirements();
        if (requirements.getOrDefault(first.getKind(), 0) >
            state.buildings.getOrDefault(first.getKind(), 0)) {
          return false;
        }
      } else {
        assert second.getAction() == BuildingQueueAction.DESTROY;
        if (!state.buildings.containsKey(second.getKind())) {
          return false;
        }
        var requirements = Item.get(first.getKind()).getBuildingsRequirements();
        int levelAfterDeconstruction = state.buildings.get(second.getKind()) - 1;
        assert levelAfterDeconstruction >= 0;
        if (requirements.getOrDefault(second.getKind(), 0) > levelAfterDeconstruction) {
          return false;
        }
      }
    }

    // Check body's fields.
    // If the second action is destroy, there will be always enough free fields after the swap.
    if (second.getAction() == BuildingQueueAction.CONSTRUCT) {
      if (first.getAction() == BuildingQueueAction.DESTROY) {
        // The destruction can free one field and thus we can construct the second one, but after the swap there may not
        // be enough fields to construct it.
        if (state.usedFields >= state.maxFields) {
          return false;
        }
      } else if ((first.getKind() == BuildingKind.TERRAFORMER ||
          first.getKind() == BuildingKind.LUNAR_BASE) && second.getKind() != BuildingKind.TERRAFORMER &&
          second.getKind() != BuildingKind.LUNAR_BASE && state.usedFields + 1 >= state.maxFields) {
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

    Iterator<BuildingQueueEntry> it = queue.values().iterator();
    BuildingKind firstKind = it.next().getKind();

    int usedFields = state.usedFields;
    int maxFields = state.maxFields;
    int level = state.buildings.getOrDefault(firstKind, 0);

    while (it.hasNext()) {
      // Check whether there is enough fields to construct the building. This must be checked, because we may remove
      // construction of a terraformer or lunar base, or remove destruction of a building.
      if (usedFields >= maxFields) {
        return false;
      }
      usedFields++;

      BuildingQueueEntry current = it.next();
      BuildingQueueAction currentAction = current.getAction();
      BuildingKind currentKind = current.getKind();

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
}
