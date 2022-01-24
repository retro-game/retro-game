package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.*;
import com.github.retro_game.retro_game.model.technology.TechnologyItem;
import com.github.retro_game.retro_game.repository.EventRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
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

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TechnologyServiceImpl implements TechnologyServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(TechnologyServiceImpl.class);
  private final int technologyQueueCapacity;
  private final ItemTimeUtils itemTimeUtils;
  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private final int maxRequiredLabLevel;
  private BodyServiceInternal bodyServiceInternal;
  private EventScheduler eventScheduler;

  public TechnologyServiceImpl(@Value("${retro-game.technology-queue-capacity}") int technologyQueueCapacity,
                               ItemTimeUtils itemTimeUtils, EventRepository eventRepository,
                               UserRepository userRepository) {
    this.technologyQueueCapacity = technologyQueueCapacity;
    this.itemTimeUtils = itemTimeUtils;
    this.eventRepository = eventRepository;
    this.userRepository = userRepository;
    this.maxRequiredLabLevel = getMaxRequiredLabLevel();
  }

  private static int getMaxRequiredLabLevel() {
    int max = 0;
    for (TechnologyItem item : TechnologyItem.getAll().values()) {
      max = Math.max(max, item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0));
    }
    return max;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Autowired
  public void setEventScheduler(EventScheduler eventScheduler) {
    this.eventScheduler = eventScheduler;
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
  public TechnologiesAndQueuePairDto getTechnologiesAndQueuePair(long bodyId) {
    var body = bodyServiceInternal.getUpdated(bodyId);
    var production = bodyServiceInternal.getProduction(body);
    var user = body.getUser();
    var state = user.getTechnologies();
    var effectiveLevelTables = getEffectiveLevelTables(user, user.getBodies().keySet());
    var queue = getQueueAndUpdateState(state, user, effectiveLevelTables);
    var technologies = getTechnologies(state, body, production, effectiveLevelTables, queue.size());
    return new TechnologiesAndQueuePairDto(technologies, queue);
  }

  private List<TechnologyQueueEntryDto> getQueueAndUpdateState(EnumMap<TechnologyKind, Integer> state, User user,
                                                               Map<Long, int[]> effectiveLevelTables) {
    var queue = user.getTechnologyQueue();
    var ret = new ArrayList<TechnologyQueueEntryDto>(queue.size());

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
      var curBodyId = cur.getValue().bodyId();

      var curLevelFrom = state.getOrDefault(curKind, 0);
      var curLevelTo = curLevelFrom + 1;
      var curCost = ItemCostUtils.getCost(curKind, curLevelTo);
      var curEnergy = ItemCostUtils.getRequiredEnergy(curKind, curLevelTo);

      var requiredLabLevel = Item.get(curKind).getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
      var effectiveLabLevel = effectiveLevelTables.get(curBodyId)[requiredLabLevel];
      var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);

      if (first) {
        var event = eventRepository.findFirstByKindAndParam(EventKind.TECHNOLOGY_QUEUE, user.getId());
        Assert.isTrue(event.isPresent(), "Event must be present");
        finishAt = event.get().getAt().toInstant().getEpochSecond();
      } else {
        var requiredTime = itemTimeUtils.getTechnologyResearchTime(curCost, effectiveLabLevel, irnLevel);
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
        var nextBodyId = next.getValue().bodyId();

        var nextLevelFrom = state.getOrDefault(nextKind, 0);
        var nextLevelTo = nextLevelFrom + 1;
        var nextCost = ItemCostUtils.getCost(nextKind, nextLevelTo);
        var nextEnergy = ItemCostUtils.getRequiredEnergy(nextKind, nextLevelTo);

        var nextBody = bodyServiceInternal.getUpdated(nextBodyId);
        if (curBodyId == nextBodyId) {
          // If we cancel the first entry, we will get some resources back. Thus, we can use these resources to
          // research the second one, because both are being researched on the same planet.
          nextCost.sub(curCost);
        }
        if (!nextBody.getResources().greaterOrEqual(nextCost)) {
          downMovable = cancelable = false;
        }

        if (nextEnergy > 0) {
          var energy = bodyServiceInternal.getProduction(nextBody).totalEnergy();
          if (nextEnergy > energy) {
            downMovable = cancelable = false;
          }
        }
      }

      var entry =
          new TechnologyQueueEntryDto(Converter.convert(curKind), cur.getKey(), curLevelTo, Converter.convert(curCost),
              curEnergy, curBodyId, effectiveLabLevel, Date.from(Instant.ofEpochSecond(finishAt)), downMovable,
              upMovable, cancelable);
      ret.add(entry);

      // Update the state.
      state.put(curKind, curLevelTo);

      first = false;
      upMovable = downMovable;
    } while (next != null);

    return ret;
  }

  private List<TechnologyDto> getTechnologies(EnumMap<TechnologyKind, Integer> state, Body body,
                                              ProductionDto production, Map<Long, int[]> effectiveLevelTables,
                                              int queueSize) {
    var user = body.getUser();
    var effectiveLevelTable = effectiveLevelTables.get(body.getId());
    var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);

    var technologies = new ArrayList<TechnologyDto>(TechnologyKindDto.values().length);
    for (var entry : TechnologyItem.getAll().entrySet()) {
      var kind = entry.getKey();
      var item = entry.getValue();

      var currentLevel = user.getTechnologyLevel(kind);
      var futureLevel = state.getOrDefault(kind, 0);

      var meetsRequirements = ItemRequirementsUtils.meetsBuildingsRequirements(item, body) &&
          ItemRequirementsUtils.meetsTechnologiesRequirements(item, state);

      var show = futureLevel > 0 || meetsRequirements;
      if (!show) {
        continue;
      }

      var nextLevel = futureLevel + 1;
      var cost = ItemCostUtils.getCost(kind, nextLevel);
      var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, nextLevel);

      var requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
      var effectiveLabLevel = effectiveLevelTable[requiredLabLevel];
      var researchTime = itemTimeUtils.getTechnologyResearchTime(cost, effectiveLabLevel, irnLevel);

      var missingResources = new Resources(cost);
      missingResources.sub(body.getResources());
      missingResources.max(0.0);
      var neededSmallCargoes = ItemUtils.calcNumUnitsForCapacity(UnitKind.SMALL_CARGO, missingResources);
      var neededLargeCargoes = ItemUtils.calcNumUnitsForCapacity(UnitKind.LARGE_CARGO, missingResources);
      var accumulationTime = ItemTimeUtils.calcAccumulationTime(body.getUpdatedAt(), missingResources, production);

      var isQueueNotFull = queueSize < technologyQueueCapacity;
      var hasEnoughResources = body.getResources().greaterOrEqual(cost);
      var hasEnoughEnergy = production.totalEnergy() >= requiredEnergy;
      var canResearchNow =
          isQueueNotFull && meetsRequirements && (queueSize > 0 || (hasEnoughResources && hasEnoughEnergy));

      var technology =
          new TechnologyDto(Converter.convert(kind), currentLevel, futureLevel, Converter.convert(cost), requiredEnergy,
              researchTime, effectiveLabLevel, Converter.convert(missingResources), neededSmallCargoes,
              neededLargeCargoes, accumulationTime, canResearchNow);
      technologies.add(technology);
    }

    return technologies;
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void research(long bodyId, TechnologyKindDto kind) {
    TechnologyKind k = Converter.convert(kind);

    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    var queue = user.getTechnologyQueue();
    if (queue.size() >= technologyQueueCapacity) {
      logger.info("Researching technology failed, queue is full: bodyId={} kind={}", bodyId, k);
      throw new QueueFullException();
    }

    Body body = bodyServiceInternal.getUpdated(bodyId);

    var futureTechs = user.getTechnologies();
    queue.values().stream().map(TechnologyQueueEntry::kind)
        .forEach(techKind -> futureTechs.put(techKind, futureTechs.get(techKind) + 1));

    var item = Item.get(k);
    if ((queue.isEmpty() && !ItemRequirementsUtils.meetsBuildingsRequirements(item, body)) ||
        !ItemRequirementsUtils.meetsTechnologiesRequirements(item, futureTechs)) {
      logger.info("Researching technology failed, requirements not met: bodyId={} kind={}", bodyId, k);
      throw new RequirementsNotMetException();
    }

    var sequenceNumber = 1;
    if (!queue.isEmpty()) {
      sequenceNumber = queue.lastKey() + 1;
      logger.info("Researching technology successful, appending to queue: bodyId={} kind={} sequenceNumber={}", bodyId,
          k, sequenceNumber);
    } else {
      var level = futureTechs.get(k) + 1;

      var cost = ItemCostUtils.getCost(k, level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.info("Researching technology failed, not enough resources: bodyId={} kind={}", bodyId, k);
        throw new NotEnoughResourcesException();
      }
      body.getResources().sub(cost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(k, level);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(body).totalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.info("Researching technology failed, not enough energy: bodyId={} kind={}", bodyId, k);
          throw new NotEnoughEnergyException();
        }
      }

      int[] table = getEffectiveLevelTables(user, Collections.singletonList(bodyId)).get(bodyId);
      int requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
      int effectiveLabLevel = table[requiredLabLevel];
      var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);

      var requiredTime = itemTimeUtils.getTechnologyResearchTime(cost, effectiveLabLevel, irnLevel);

      logger.info("Researching technology successful, creating a new event: bodyId={} kind={}", bodyId, k);
      Date now = body.getUpdatedAt();
      Date startAt = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      Event event = new Event();
      event.setAt(startAt);
      event.setKind(EventKind.TECHNOLOGY_QUEUE);
      event.setParam(userId);
      eventScheduler.schedule(event);
    }

    var entry = new TechnologyQueueEntry(k, bodyId);
    queue.put(sequenceNumber, entry);
    user.setTechnologyQueue(queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void moveDown(long bodyId, int sequenceNumber) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    var queue = user.getTechnologyQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.info("Moving down entry in technology queue failed, no such queue entry: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    var head = queue.headMap(sequenceNumber);
    var tail = queue.tailMap(sequenceNumber);

    var futureTechs = user.getTechnologies();
    head.values().stream().map(TechnologyQueueEntry::kind)
        .forEach(techKind -> futureTechs.put(techKind, futureTechs.get(techKind) + 1));

    if (!canSwapTop(futureTechs, tail)) {
      logger.info("Moving down entry in technology queue failed, cannot swap top: userId={} sequenceNumber={}", userId,
          sequenceNumber);
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
      logger.info("Moving down entry in technology queue successful, the entry isn't the first: userId={}" +
          " sequenceNumber={}", userId, sequenceNumber);
    } else {
      // The first entry.

      Body firstBody = bodyServiceInternal.getUpdated(entry.bodyId());
      TechnologyKind firstKind = entry.kind();
      var firstLevel = futureTechs.get(firstKind) + 1;
      var firstCost = ItemCostUtils.getCost(firstKind, firstLevel);

      Body secondBody = bodyServiceInternal.getUpdated(next.bodyId());
      TechnologyKind secondKind = next.kind();
      var secondLevel = futureTechs.get(secondKind) + 1;
      var secondCost = ItemCostUtils.getCost(secondKind, secondLevel);

      // If both bodies are the same, the references to resources should be the same as well.
      if (firstBody.getId() == secondBody.getId()) {
        secondBody = firstBody;
      }

      var now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
      bodyServiceInternal.updateResourcesAndShipyard(firstBody, now);
      bodyServiceInternal.updateResourcesAndShipyard(secondBody, firstBody.getUpdatedAt());

      firstBody.getResources().add(firstCost);
      if (!secondBody.getResources().greaterOrEqual(secondCost)) {
        logger.info("Moving down entry in technology queue failed, not enough resources: userId={} sequenceNumber={}",
            userId, sequenceNumber);
        throw new NotEnoughResourcesException();
      }
      secondBody.getResources().sub(secondCost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(secondKind, secondLevel);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(secondBody).totalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.info("Moving down entry in technology queue failed, not enough energy: userId={} sequenceNumber={}",
              userId, sequenceNumber);
          throw new NotEnoughEnergyException();
        }
      }

      Optional<Event> eventOptional = eventRepository.findFirstByKindAndParam(EventKind.TECHNOLOGY_QUEUE, userId);
      if (!eventOptional.isPresent()) {
        logger.error(
            "Moving down entry in technology queue failed, the event is not present: userId={}" + " sequenceNumber={}",
            userId, sequenceNumber);
        throw new MissingEventException();
      }
      Event event = eventOptional.get();

      var item = Item.get(secondKind);
      int requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
      int[] table =
          getEffectiveLevelTables(user, Collections.singletonList(secondBody.getId())).get(secondBody.getId());
      int effectiveLabLevel = table[requiredLabLevel];
      var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);
      var requiredTime = itemTimeUtils.getTechnologyResearchTime(secondCost, effectiveLabLevel, irnLevel);

      logger.info("Moving down entry in technology queue successful, the entry is the first, adding an event for the" +
          " next entry: userId={} sequenceNumber={}", userId, sequenceNumber);
      Date at = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      event.setAt(at);
      eventScheduler.schedule(event);
    }

    // Swap.
    queue.put(sequenceNumber, next);
    queue.put(nextSeq, entry);

    user.setTechnologyQueue(queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void moveUp(long bodyId, int sequenceNumber) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    var queue = user.getTechnologyQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.info("Moving up entry in technology queue failed, no such queue entry: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    var head = queue.headMap(sequenceNumber);
    if (head.isEmpty()) {
      logger.info("Moving up entry in technology queue failed, the entry is first: bodyId={} sequenceNumber={}", userId,
          sequenceNumber);
      throw new CannotMoveException();
    }

    // Moving an entry up is equivalent to moving the previous one down.
    moveDown(bodyId, head.lastKey());
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void cancel(long bodyId, int sequenceNumber) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    var queue = user.getTechnologyQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.info("Cancelling entry in technology queue failed, no such queue entry: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    var head = queue.headMap(sequenceNumber);
    var tail = queue.tailMap(sequenceNumber);

    var futureTechs = user.getTechnologies();
    head.values().stream().map(TechnologyQueueEntry::kind)
        .forEach(techKind -> futureTechs.put(techKind, futureTechs.get(techKind) + 1));

    if (!canRemoveTop(futureTechs, tail)) {
      logger.info("Cancelling entry in technology queue failed, cannot remove top: userId={} sequenceNumber={}", userId,
          sequenceNumber);
      throw new CannotCancelException();
    }

    if (!head.isEmpty()) {
      // The entry is not the first in the queue, just remove it.
      logger.info("Cancelling entry in technology queue successful, the entry isn't the first: userId={}" +
          " sequenceNumber={}", userId, sequenceNumber);
      var entry = queue.remove(sequenceNumber);
      assert entry != null;
    } else {
      // The first entry.

      var it = tail.values().iterator();

      var entry = it.next();
      var body = bodyServiceInternal.getUpdated(entry.bodyId());
      var kind = entry.kind();

      it.remove();

      var level = futureTechs.get(kind) + 1;
      var cost = ItemCostUtils.getCost(kind, level);

      body.getResources().add(cost);

      Optional<Event> eventOptional = eventRepository.findFirstByKindAndParam(EventKind.TECHNOLOGY_QUEUE, userId);
      if (!eventOptional.isPresent()) {
        logger.error(
            "Cancelling in technology queue failed, the event is not present: userId={}" + " sequenceNumber={}", userId,
            sequenceNumber);
        throw new MissingEventException();
      }
      Event event = eventOptional.get();

      if (!it.hasNext()) {
        logger.info("Cancelling entry in technology queue successful, queue is empty now: userId={} sequenceNumber={}",
            userId, sequenceNumber);
        eventRepository.delete(event);
      } else {
        Date now = body.getUpdatedAt();

        var next = it.next();
        var nextBody = bodyServiceInternal.getUpdated(next.bodyId());
        kind = next.kind();
        level = futureTechs.get(kind) + 1;
        cost = ItemCostUtils.getCost(kind, level);

        // If both bodies are the same, the references to resources should be the same as well.
        if (nextBody.getId() == body.getId()) {
          nextBody = body;
        }

        if (!nextBody.getResources().greaterOrEqual(cost)) {
          logger.info("Cancelling entry in technology queue failed, not enough resources: userId={} sequenceNumber={}",
              userId, sequenceNumber);
          throw new NotEnoughResourcesException();
        }
        nextBody.getResources().sub(cost);

        var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, level);
        if (requiredEnergy > 0) {
          int totalEnergy = bodyServiceInternal.getProduction(nextBody).totalEnergy();
          if (requiredEnergy > totalEnergy) {
            logger.info("Cancelling entry in technology queue failed, not enough energy: userId={} sequenceNumber={}",
                userId, sequenceNumber);
            throw new NotEnoughEnergyException();
          }
        }

        var item = Item.get(kind);
        int requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
        int[] table = getEffectiveLevelTables(user, Collections.singletonList(nextBody.getId())).get(nextBody.getId());
        int effectiveLabLevel = table[requiredLabLevel];
        var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);
        var requiredTime = itemTimeUtils.getTechnologyResearchTime(cost, effectiveLabLevel, irnLevel);

        logger.info("Cancelling entry in technology queue successful, the entry is the first, modifying the event:" +
            "userId={} sequenceNumber={}", userId, sequenceNumber);
        Date at = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
        event.setAt(at);
        eventScheduler.schedule(event);
      }
    }

    user.setTechnologyQueue(queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void handle(Event event) {
    long userId = event.getParam();
    var user = userRepository.getOne(userId);

    Date at = event.getAt();

    eventRepository.delete(event);

    var queue = user.getTechnologyQueue();
    var it = queue.entrySet().iterator();

    // This shouldn't happen.
    if (!it.hasNext()) {
      logger.error("Handling technology queue, queue is empty: userId={}", userId);
      return;
    }

    var n = it.next();
    var seq = n.getKey();
    var entry = n.getValue();

    it.remove();

    // Update technologies.
    var oldLevel = user.getTechnologyLevel(entry.kind());
    assert oldLevel >= 0;
    var newLevel = oldLevel + 1;
    logger.info("Handling technology queue, updating technology level: userId={} kind={} oldLevel={} newLevel={}",
        userId, entry.kind(), oldLevel, newLevel);
    user.setTechnologyLevel(entry.kind(), newLevel);

    while (it.hasNext()) {
      n = it.next();
      seq = n.getKey();
      entry = n.getValue();

      var level = user.getTechnologyLevel(entry.kind()) + 1;

      var body = bodyServiceInternal.getUpdated(entry.bodyId());

      var cost = ItemCostUtils.getCost(entry.kind(), level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.info("Handling technology queue, removing entry, not enough resources: userId={} bodyId={} kind={}" +
            " sequenceNumber={}", userId, entry.bodyId(), entry.kind(), seq);
        it.remove();
        continue;
      }

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(entry.kind(), level);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(body).totalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.info("Handling technology queue, removing entry, not enough energy: userId={} bodyId={} kind={}" +
              " sequenceNumber={}", userId, entry.bodyId(), entry.kind(), seq);
          it.remove();
          continue;
        }
      }

      var item = Item.get(entry.kind());
      if (!ItemRequirementsUtils.meetsRequirements(item, body)) {
        logger.info("Handling technology queue, removing entry, requirements not met: userId={} bodyId={} kind={}" +
            " sequenceNumber={}", userId, entry.bodyId(), entry.kind(), seq);
        it.remove();
        continue;
      }

      logger.info("Handling technology queue, creating an event: userId={} bodyId={} kind={} sequenceNumber={}", userId,
          entry.bodyId(), entry.kind(), seq);

      body.getResources().sub(cost);

      int[] table = getEffectiveLevelTables(user, Collections.singletonList(entry.bodyId())).get(entry.bodyId());
      int requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
      int effectiveLabLevel = table[requiredLabLevel];
      var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);
      var requiredTime = itemTimeUtils.getTechnologyResearchTime(cost, effectiveLabLevel, irnLevel);
      Date startAt = Date.from(Instant.ofEpochSecond(at.toInstant().getEpochSecond() + requiredTime));

      Event newEvent = new Event();
      newEvent.setAt(startAt);
      newEvent.setKind(EventKind.TECHNOLOGY_QUEUE);
      newEvent.setParam(userId);
      eventScheduler.schedule(newEvent);

      break;
    }

    user.setTechnologyQueue(queue);
  }

  private Map<Long, int[]> getEffectiveLevelTables(User user, Collection<Long> bodiesIds) {
    var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);

    var bodies = user.getBodies();
    var labs = bodies.entrySet().stream()
        .map(entry -> Tuple.of(entry.getKey(), entry.getValue().getBuildingLevel(BuildingKind.RESEARCH_LAB)))
        .sorted(Comparator.comparingInt(Tuple2<Long, Integer>::_2).reversed()).collect(Collectors.toList());

    Map<Long, int[]> tables = new HashMap<>(bodiesIds.size());
    for (long bodyId : bodiesIds) {
      var currentBodyLabLevel = bodies.get(bodyId).getBuildingLevel(BuildingKind.RESEARCH_LAB);

      int[] table = new int[maxRequiredLabLevel + 1];
      Arrays.fill(table, 0, Math.min(maxRequiredLabLevel, currentBodyLabLevel) + 1, currentBodyLabLevel);
      labs.stream().filter(tuple -> tuple._1 != bodyId).limit(irnLevel).mapToInt(Tuple2::_2).forEach(level -> {
        for (int i = Math.min(maxRequiredLabLevel, level); i >= 0; i--) {
          if (table[i] != 0) {
            table[i] += level;
          }
        }
      });

      tables.put(bodyId, table);
    }
    return tables;
  }

  // Checks whether it is possible to swap top two items in the queue ignoring resources.
  private boolean canSwapTop(Map<TechnologyKind, Integer> techs, SortedMap<Integer, TechnologyQueueEntry> queue) {
    if (queue.size() < 2) {
      return false;
    }

    // Get first two items.
    Iterator<TechnologyQueueEntry> it = queue.values().iterator();
    TechnologyQueueEntry first = it.next();
    TechnologyQueueEntry second = it.next();

    // Check whether the second one depends on the first one.
    var requirements = Item.get(second.kind()).getTechnologiesRequirements();
    return techs.get(first.kind()) >= requirements.getOrDefault(first.kind(), 0);
  }

  private boolean canRemoveTop(Map<TechnologyKind, Integer> techs, SortedMap<Integer, TechnologyQueueEntry> queue) {
    if (queue.isEmpty()) {
      return false;
    }

    Iterator<TechnologyQueueEntry> it = queue.values().iterator();
    TechnologyKind firstKind = it.next().kind();
    var level = techs.get(firstKind);

    while (it.hasNext()) {
      TechnologyQueueEntry current = it.next();
      TechnologyKind currentKind = current.kind();

      if (currentKind == firstKind) {
        level++;
      } else {
        var requirements = Item.get(currentKind).getTechnologiesRequirements();
        if (requirements.getOrDefault(firstKind, 0) > level) {
          return false;
        }
      }
    }

    return true;
  }
}
