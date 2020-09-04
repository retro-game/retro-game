package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.TechnologiesAndQueuePairDto;
import com.github.retro_game.retro_game.dto.TechnologyDto;
import com.github.retro_game.retro_game.dto.TechnologyKindDto;
import com.github.retro_game.retro_game.dto.TechnologyQueueEntryDto;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.Item;
import com.github.retro_game.retro_game.model.ItemCostUtils;
import com.github.retro_game.retro_game.model.ItemRequirementsUtils;
import com.github.retro_game.retro_game.model.ItemTimeUtils;
import com.github.retro_game.retro_game.model.technology.TechnologyItem;
import com.github.retro_game.retro_game.repository.EventRepository;
import com.github.retro_game.retro_game.repository.TechnologyQueueEntryRepository;
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

@Service("technologyService")
class TechnologyServiceImpl implements TechnologyServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(TechnologyServiceImpl.class);
  private final int technologyQueueCapacity;
  private final ItemTimeUtils itemTimeUtils;
  private final EventRepository eventRepository;
  private final TechnologyQueueEntryRepository technologyQueueEntryRepository;
  private final UserRepository userRepository;
  private final int maxRequiredLabLevel;
  private BodyServiceInternal bodyServiceInternal;
  private EventScheduler eventScheduler;

  public TechnologyServiceImpl(@Value("${retro-game.technology-queue-capacity}") int technologyQueueCapacity,
                               ItemTimeUtils itemTimeUtils, EventRepository eventRepository,
                               TechnologyQueueEntryRepository technologyQueueEntryRepository,
                               UserRepository userRepository) {
    this.technologyQueueCapacity = technologyQueueCapacity;
    this.itemTimeUtils = itemTimeUtils;
    this.eventRepository = eventRepository;
    this.technologyQueueEntryRepository = technologyQueueEntryRepository;
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
  public int getLevel(long bodyId, TechnologyKindDto kind) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return user.getTechnologyLevel(Converter.convert(kind));
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
  public TechnologiesAndQueuePairDto getTechnologiesAndQueuePair(long bodyId) {
    Body body = bodyServiceInternal.getUpdated(bodyId);

    User user = body.getUser();
    long userId = user.getId();

    Map<Long, int[]> effectiveLevelTables = getEffectiveLevelTables(user, user.getBodies().keySet());

    var futureTechs = user.getTechnologies();

    SortedMap<Integer, TechnologyQueueEntry> techQueue = user.getTechnologyQueue();
    int size = techQueue.size();
    List<TechnologyQueueEntryDto> queue = new ArrayList<>(size);
    if (size > 0) {
      Iterator<Map.Entry<Integer, TechnologyQueueEntry>> it = techQueue.entrySet().iterator();
      Map.Entry<Integer, TechnologyQueueEntry> next = it.next();
      boolean first = true;
      long finishAt = 0;
      boolean upMovable = false;
      do {
        Map.Entry<Integer, TechnologyQueueEntry> entry = next;
        next = it.hasNext() ? it.next() : null;

        TechnologyQueueEntry queueEntry = entry.getValue();
        TechnologyKind kind = queueEntry.getKind();

        var level = futureTechs.get(kind) + 1;
        var cost = ItemCostUtils.getCost(kind, level);
        var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, level);

        Body entryBody = queueEntry.getBody();
        var item = Item.get(kind);
        int requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
        int effectiveLabLevel = effectiveLevelTables.get(entryBody.getId())[requiredLabLevel];
        var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);

        long requiredTime;
        if (first) {
          Optional<Event> event = eventRepository.findFirstByKindAndParam(EventKind.TECHNOLOGY_QUEUE, userId);
          Assert.isTrue(event.isPresent(), "Event must be present");
          finishAt = event.get().getAt().toInstant().getEpochSecond();
          long now = body.getUpdatedAt().toInstant().getEpochSecond();
          requiredTime = finishAt - now;
        } else {
          requiredTime = itemTimeUtils.getTechnologyResearchTime(cost, effectiveLabLevel, irnLevel);
          finishAt += requiredTime;
        }

        // Check dependencies of subsequent entries.
        SortedMap<Integer, TechnologyQueueEntry> tail = techQueue.tailMap(entry.getKey());
        boolean downMovable = canSwapTop(futureTechs, tail);
        boolean cancelable = canRemoveTop(futureTechs, tail);

        // If we cancel the first entry we immediately start the second one, thus we need to check resources.
        if (first && next != null) {
          Body currentBody = entry.getValue().getBody();
          Body nextBody = next.getValue().getBody();
          TechnologyKind nextKind = next.getValue().getKind();
          var nextLevel = futureTechs.get(nextKind) + 1;
          var nextCost = ItemCostUtils.getCost(nextKind, nextLevel);
          if (currentBody.getId() == nextBody.getId()) {
            // If we cancel the first entry, we will getSlots some resources back. Thus we can use these resources to
            // research the second one, because both are being researched on the same body.
            nextCost.sub(cost);
          }
          if (!nextBody.getResources().greaterOrEqual(nextCost)) {
            downMovable = cancelable = false;
          }

          var nextRequiredEnergy = ItemCostUtils.getRequiredEnergy(nextKind, nextLevel);
          if (nextRequiredEnergy > 0) {
            int totalEnergy = bodyServiceInternal.getProduction(nextBody).getTotalEnergy();
            if (nextRequiredEnergy > totalEnergy) {
              downMovable = cancelable = false;
            }
          }
        }

        queue.add(new TechnologyQueueEntryDto(Converter.convert(kind), entry.getKey(), level, Converter.convert(cost),
            requiredEnergy, entryBody.getId(), entryBody.getName(), Converter.convert(entryBody.getCoordinates()),
            effectiveLabLevel, Date.from(Instant.ofEpochSecond(finishAt)), requiredTime, downMovable, upMovable,
            cancelable));

        futureTechs.put(kind, futureTechs.get(kind) + 1);

        first = false;
        upMovable = downMovable;
      } while (next != null);
    }

    Resources resources = body.getResources();
    int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();
    boolean canResearch = queue.size() < technologyQueueCapacity;
    int[] currentBodyTable = effectiveLevelTables.get(bodyId);
    List<TechnologyDto> techs = new ArrayList<>();
    for (Map.Entry<TechnologyKind, TechnologyItem> entry : TechnologyItem.getAll().entrySet()) {
      TechnologyKind kind = entry.getKey();
      TechnologyItem item = entry.getValue();
      boolean meetsRequirements = ItemRequirementsUtils.meetsBuildingsRequirements(item, body) &&
          ItemRequirementsUtils.meetsTechnologiesRequirements(item, futureTechs);
      var futureLevel = futureTechs.get(kind);
      if (meetsRequirements || futureLevel > 0) {
        var currentLevel = user.getTechnologyLevel(kind);

        var cost = ItemCostUtils.getCost(kind, futureLevel + 1);
        var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, futureLevel + 1);

        int requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
        int effectiveLabLevel = currentBodyTable[requiredLabLevel];
        var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);

        long researchTime = itemTimeUtils.getTechnologyResearchTime(cost, effectiveLabLevel, irnLevel);

        boolean canResearchNow = canResearch && meetsRequirements &&
            (!queue.isEmpty() || (resources.greaterOrEqual(cost) && totalEnergy >= requiredEnergy));

        techs.add(new TechnologyDto(Converter.convert(kind), currentLevel, futureLevel, Converter.convert(cost),
            requiredEnergy, researchTime, effectiveLabLevel, canResearchNow));
      }
    }
    // Keep the order defined in the service layer.
    techs.sort(Comparator.comparing(TechnologyDto::getKind));

    return new TechnologiesAndQueuePairDto(techs, queue);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void research(long bodyId, TechnologyKindDto kind) {
    TechnologyKind k = Converter.convert(kind);

    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    SortedMap<Integer, TechnologyQueueEntry> queue = user.getTechnologyQueue();
    if (queue.size() >= technologyQueueCapacity) {
      logger.warn("Researching technology failed, queue is full: bodyId={} kind={}", bodyId, k);
      throw new QueueFullException();
    }

    Body body = bodyServiceInternal.getUpdated(bodyId);

    var futureTechs = user.getTechnologies();
    queue.values().stream()
        .map(TechnologyQueueEntry::getKind)
        .forEach(techKind -> futureTechs.put(techKind, futureTechs.get(techKind) + 1));

    var item = Item.get(k);
    if ((queue.isEmpty() && !ItemRequirementsUtils.meetsBuildingsRequirements(item, body)) ||
        !ItemRequirementsUtils.meetsTechnologiesRequirements(item, futureTechs)) {
      logger.warn("Researching technology failed, requirements not met: bodyId={} kind={}", bodyId, k);
      throw new RequirementsNotMetException();
    }

    TechnologyQueueEntryKey key = new TechnologyQueueEntryKey();
    key.setUser(user);
    if (!queue.isEmpty()) {
      int sequenceNumber = queue.lastKey() + 1;
      key.setSequence(sequenceNumber);
      logger.info("Researching technology successful, appending to queue: bodyId={} kind={} sequenceNumber={}",
          bodyId, k, sequenceNumber);
    } else {
      var level = futureTechs.get(k) + 1;

      var cost = ItemCostUtils.getCost(k, level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.warn("Researching technology failed, not enough resources: bodyId={} kind={}", bodyId, k);
        throw new NotEnoughResourcesException();
      }
      body.getResources().sub(cost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(k, level);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.warn("Researching technology failed, not enough energy: bodyId={} kind={}", bodyId, k);
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

      key.setSequence(1);
    }

    TechnologyQueueEntry entry = new TechnologyQueueEntry();
    entry.setKey(key);
    entry.setBody(body);
    entry.setKind(k);
    technologyQueueEntryRepository.save(entry);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void moveDown(long bodyId, int sequenceNumber) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    SortedMap<Integer, TechnologyQueueEntry> queue = user.getTechnologyQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.warn("Moving down entry in technology queue failed, no such queue entry: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    SortedMap<Integer, TechnologyQueueEntry> head = queue.headMap(sequenceNumber);
    SortedMap<Integer, TechnologyQueueEntry> tail = queue.tailMap(sequenceNumber);

    var futureTechs = user.getTechnologies();
    head.values().stream()
        .map(TechnologyQueueEntry::getKind)
        .forEach(techKind -> futureTechs.put(techKind, futureTechs.get(techKind) + 1));

    if (!canSwapTop(futureTechs, tail)) {
      logger.warn("Moving down entry in technology queue failed, cannot swap top: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      throw new CannotMoveException();
    }

    // canSwapTop == true implies tail.size >= 2.
    assert tail.size() >= 2;
    Iterator<TechnologyQueueEntry> it = tail.values().iterator();
    TechnologyQueueEntry entry = it.next();
    TechnologyQueueEntry next = it.next();

    if (!head.isEmpty()) {
      // The entry is not the first in the queue, just swap it with the next.
      logger.info("Moving down entry in technology queue successful, the entry isn't the first: userId={}" +
              " sequenceNumber={}",
          userId, sequenceNumber);
    } else {
      // The first entry.

      Body firstBody = entry.getBody();
      TechnologyKind firstKind = entry.getKind();
      var firstLevel = futureTechs.get(firstKind) + 1;
      var firstCost = ItemCostUtils.getCost(firstKind, firstLevel);

      Body secondBody = next.getBody();
      TechnologyKind secondKind = next.getKind();
      var secondLevel = futureTechs.get(secondKind) + 1;
      var secondCost = ItemCostUtils.getCost(secondKind, secondLevel);

      // If both bodies are the same, the references to resources should be the same as well.
      if (firstBody.getId() == secondBody.getId()) {
        secondBody = firstBody;
      }

      bodyServiceInternal.updateResources(firstBody, null);
      bodyServiceInternal.updateResources(secondBody, firstBody.getUpdatedAt());

      firstBody.getResources().add(firstCost);
      if (!secondBody.getResources().greaterOrEqual(secondCost)) {
        logger.warn("Moving down entry in technology queue failed, not enough resources: userId={} sequenceNumber={}",
            userId, sequenceNumber);
        throw new NotEnoughResourcesException();
      }
      secondBody.getResources().sub(secondCost);

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(secondKind, secondLevel);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(secondBody).getTotalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.warn("Moving down entry in technology queue failed, not enough energy: userId={} sequenceNumber={}",
              userId, sequenceNumber);
          throw new NotEnoughEnergyException();
        }
      }

      Optional<Event> eventOptional = eventRepository.findFirstByKindAndParam(EventKind.TECHNOLOGY_QUEUE, userId);
      if (!eventOptional.isPresent()) {
        logger.error("Moving down entry in technology queue failed, the event is not present: userId={}" +
                " sequenceNumber={}",
            userId, sequenceNumber);
        throw new MissingEventException();
      }
      Event event = eventOptional.get();

      var item = Item.get(secondKind);
      int requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
      int[] table = getEffectiveLevelTables(user, Collections.singletonList(secondBody.getId()))
          .get(secondBody.getId());
      int effectiveLabLevel = table[requiredLabLevel];
      var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);
      var requiredTime = itemTimeUtils.getTechnologyResearchTime(secondCost, effectiveLabLevel, irnLevel);

      logger.info("Moving down entry in technology queue successful, the entry is the first, adding an event for the" +
              " next entry: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      Date now = secondBody.getUpdatedAt();
      Date at = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
      event.setAt(at);
      eventScheduler.schedule(event);
    }

    // Swap.
    Body body = entry.getBody();
    TechnologyKind kind = entry.getKind();
    entry.setBody(next.getBody());
    entry.setKind(next.getKind());
    next.setBody(body);
    next.setKind(kind);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void moveUp(long bodyId, int sequenceNumber) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    SortedMap<Integer, TechnologyQueueEntry> queue = user.getTechnologyQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.warn("Moving up entry in technology queue failed, no such queue entry: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    SortedMap<Integer, TechnologyQueueEntry> head = queue.headMap(sequenceNumber);
    if (head.isEmpty()) {
      logger.warn("Moving up entry in technology queue failed, the entry is first: bodyId={} sequenceNumber={}",
          userId, sequenceNumber);
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

    SortedMap<Integer, TechnologyQueueEntry> queue = user.getTechnologyQueue();
    if (!queue.containsKey(sequenceNumber)) {
      logger.warn("Cancelling entry in technology queue failed, no such queue entry: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      throw new NoSuchQueueEntryException();
    }

    SortedMap<Integer, TechnologyQueueEntry> head = queue.headMap(sequenceNumber);
    SortedMap<Integer, TechnologyQueueEntry> tail = queue.tailMap(sequenceNumber);

    var futureTechs = user.getTechnologies();
    head.values().stream()
        .map(TechnologyQueueEntry::getKind)
        .forEach(techKind -> futureTechs.put(techKind, futureTechs.get(techKind) + 1));

    if (!canRemoveTop(futureTechs, tail)) {
      logger.warn("Cancelling entry in technology queue failed, cannot remove top: userId={} sequenceNumber={}",
          userId, sequenceNumber);
      throw new CannotCancelException();
    }

    if (!head.isEmpty()) {
      // The entry is not the first in the queue, just remove it.
      logger.info("Cancelling entry in technology queue successful, the entry isn't the first: userId={}" +
              " sequenceNumber={}",
          userId, sequenceNumber);
      TechnologyQueueEntry entry = queue.remove(sequenceNumber);
      technologyQueueEntryRepository.delete(entry);
    } else {
      // The first entry.

      Iterator<TechnologyQueueEntry> it = tail.values().iterator();

      TechnologyQueueEntry entry = it.next();
      Body body = entry.getBody();
      TechnologyKind kind = entry.getKind();
      var level = futureTechs.get(kind) + 1;
      var cost = ItemCostUtils.getCost(kind, level);

      bodyServiceInternal.updateResources(body, null);
      body.getResources().add(cost);

      Optional<Event> eventOptional = eventRepository.findFirstByKindAndParam(EventKind.TECHNOLOGY_QUEUE, userId);
      if (!eventOptional.isPresent()) {
        logger.error("Cancelling in technology queue failed, the event is not present: userId={}" +
                " sequenceNumber={}",
            userId, sequenceNumber);
        throw new MissingEventException();
      }
      Event event = eventOptional.get();

      if (!it.hasNext()) {
        logger.info("Cancelling entry in technology queue successful, queue is empty now: userId={} sequenceNumber={}",
            userId, sequenceNumber);
        eventRepository.delete(event);
      } else {
        Date now = body.getUpdatedAt();

        TechnologyQueueEntry next = it.next();
        Body nextBody = next.getBody();
        kind = next.getKind();
        level = futureTechs.get(kind) + 1;
        cost = ItemCostUtils.getCost(kind, level);

        // If both bodies are the same, the references to resources should be the same as well.
        if (nextBody.getId() == body.getId()) {
          nextBody = body;
        }

        bodyServiceInternal.updateResources(nextBody, now);
        if (!nextBody.getResources().greaterOrEqual(cost)) {
          logger.warn("Cancelling entry in technology queue failed, not enough resources: userId={} sequenceNumber={}",
              userId, sequenceNumber);
          throw new NotEnoughResourcesException();
        }
        nextBody.getResources().sub(cost);

        var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, level);
        if (requiredEnergy > 0) {
          int totalEnergy = bodyServiceInternal.getProduction(nextBody).getTotalEnergy();
          if (requiredEnergy > totalEnergy) {
            logger.warn("Cancelling entry in technology queue failed, not enough energy: userId={} sequenceNumber={}",
                userId, sequenceNumber);
            throw new NotEnoughEnergyException();
          }
        }

        var item = Item.get(kind);
        int requiredLabLevel = item.getBuildingsRequirements().getOrDefault(BuildingKind.RESEARCH_LAB, 0);
        int[] table = getEffectiveLevelTables(user, Collections.singletonList(nextBody.getId()))
            .get(nextBody.getId());
        int effectiveLabLevel = table[requiredLabLevel];
        var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);
        var requiredTime = itemTimeUtils.getTechnologyResearchTime(cost, effectiveLabLevel, irnLevel);

        logger.info("Cancelling entry in technology queue successful, the entry is the first, modifying the event:" +
                "userId={} sequenceNumber={}",
            userId, sequenceNumber);
        Date at = Date.from(Instant.ofEpochSecond(now.toInstant().getEpochSecond() + requiredTime));
        event.setAt(at);
        eventScheduler.schedule(event);
      }

      it.remove();
      technologyQueueEntryRepository.delete(entry);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void handle(Event event) {
    long userId = event.getParam();
    User user = userRepository.getOne(userId);

    Date at = event.getAt();

    eventRepository.delete(event);

    Collection<TechnologyQueueEntry> values = user.getTechnologyQueue().values();
    Iterator<TechnologyQueueEntry> it = values.iterator();

    // This shouldn't happen.
    if (!it.hasNext()) {
      logger.error("Handling technology queue, queue is empty: userId={}", userId);
      return;
    }

    TechnologyQueueEntry entry = it.next();
    TechnologyKind kind = entry.getKind();

    it.remove();
    technologyQueueEntryRepository.delete(entry);

    // Update technologies.
    var oldLevel = user.getTechnologyLevel(kind);
    assert oldLevel >= 0;
    var newLevel = oldLevel + 1;
    logger.info("Handling technology queue, updating technology level: userId={} kind={} oldLevel={} newLevel={}",
        userId, kind, oldLevel, newLevel);
    user.setTechnologyLevel(kind, newLevel);

    while (it.hasNext()) {
      entry = it.next();
      kind = entry.getKind();
      int sequenceNumber = entry.getSequence();

      var level = user.getTechnologyLevel(kind) + 1;

      Body body = entry.getBody();
      long bodyId = body.getId();
      bodyServiceInternal.updateResources(body, at);

      var cost = ItemCostUtils.getCost(kind, level);
      if (!body.getResources().greaterOrEqual(cost)) {
        logger.info("Handling technology queue, removing entry, not enough resources: userId={} bodyId={} kind={}" +
                " sequenceNumber={}",
            userId, bodyId, kind, sequenceNumber);
        it.remove();
        technologyQueueEntryRepository.delete(entry);
        continue;
      }

      var requiredEnergy = ItemCostUtils.getRequiredEnergy(kind, level);
      if (requiredEnergy > 0) {
        int totalEnergy = bodyServiceInternal.getProduction(body).getTotalEnergy();
        if (requiredEnergy > totalEnergy) {
          logger.info("Handling technology queue, removing entry, not enough energy: userId={} bodyId={} kind={}" +
                  " sequenceNumber={}",
              userId, bodyId, kind, sequenceNumber);
          it.remove();
          technologyQueueEntryRepository.delete(entry);
          continue;
        }
      }

      var item = Item.get(kind);
      if (!ItemRequirementsUtils.meetsRequirements(item, body)) {
        logger.info("Handling technology queue, removing entry, requirements not met: userId={} bodyId={} kind={}" +
                " sequenceNumber={}",
            userId, bodyId, kind, sequenceNumber);
        it.remove();
        technologyQueueEntryRepository.delete(entry);
        continue;
      }

      logger.info("Handling technology queue, creating an event: userId={} bodyId={} kind={} sequenceNumber={}",
          userId, bodyId, kind, sequenceNumber);

      body.getResources().sub(cost);

      int[] table = getEffectiveLevelTables(user, Collections.singletonList(bodyId)).get(bodyId);
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
  }

  private Map<Long, int[]> getEffectiveLevelTables(User user, Collection<Long> bodiesIds) {
    var irnLevel = user.getTechnologyLevel(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK);

    var bodies = user.getBodies();
    var labs = bodies.entrySet().stream()
        .map(entry -> Tuple.of(entry.getKey(), entry.getValue().getBuildingLevel(BuildingKind.RESEARCH_LAB)))
        .sorted(Comparator.comparingInt(Tuple2<Long, Integer>::_2).reversed())
        .collect(Collectors.toList());

    Map<Long, int[]> tables = new HashMap<>(bodiesIds.size());
    for (long bodyId : bodiesIds) {
      var currentBodyLabLevel = bodies.get(bodyId).getBuildingLevel(BuildingKind.RESEARCH_LAB);

      int[] table = new int[maxRequiredLabLevel + 1];
      Arrays.fill(table, 0, Math.min(maxRequiredLabLevel, currentBodyLabLevel) + 1, currentBodyLabLevel);
      labs.stream()
          .filter(tuple -> tuple._1 != bodyId)
          .limit(irnLevel)
          .mapToInt(Tuple2::_2)
          .forEach(level -> {
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
    var requirements = Item.get(second.getKind()).getTechnologiesRequirements();
    return techs.get(first.getKind()) >= requirements.getOrDefault(first.getKind(), 0);
  }

  private boolean canRemoveTop(Map<TechnologyKind, Integer> techs, SortedMap<Integer, TechnologyQueueEntry> queue) {
    if (queue.isEmpty()) {
      return false;
    }

    Iterator<TechnologyQueueEntry> it = queue.values().iterator();
    TechnologyKind firstKind = it.next().getKind();
    var level = techs.get(firstKind);

    while (it.hasNext()) {
      TechnologyQueueEntry current = it.next();
      TechnologyKind currentKind = current.getKind();

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
