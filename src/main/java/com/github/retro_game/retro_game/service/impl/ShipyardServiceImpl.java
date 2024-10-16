package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.Item;
import com.github.retro_game.retro_game.model.ItemRequirementsUtils;
import com.github.retro_game.retro_game.model.ItemTimeUtils;
import com.github.retro_game.retro_game.model.ItemUtils;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.service.exception.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShipyardServiceImpl implements ShipyardServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(ShipyardServiceImpl.class);
  private final ItemTimeUtils itemTimeUtils;
  private BodyServiceInternal bodyServiceInternal;

  public ShipyardServiceImpl(ItemTimeUtils itemTimeUtils) {
    this.itemTimeUtils = itemTimeUtils;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
  public UnitsAndQueuePairDto getUnitsAndQueuePair(long bodyId, UnitTypeDto type) {
    var body = bodyServiceInternal.getUpdated(bodyId);
    var production = bodyServiceInternal.getProduction(body);
    var state = new EnumMap<UnitKind, Integer>(UnitKind.class);
    var queue = getQueueAndUpdateState(state, body);
    var units = getUnits(state, body, production, type);
    return new UnitsAndQueuePairDto(units, queue);
  }

  private List<ShipyardQueueEntryDto> getQueueAndUpdateState(EnumMap<UnitKind, Integer> state, Body body) {
    var queue = body.getShipyardQueue();
    var ret = new ArrayList<ShipyardQueueEntryDto>(queue.size());

    var first = true;
    var finishAt = 0L;
    for (var entry : queue) {
      var kind = entry.kind();
      var count = entry.count();
      assert count >= 1;

      var item = Item.get(kind);

      var unitCost = item.getCost();
      var totalCost = new Resources(unitCost);
      totalCost.mul(count);

      var constructionTime = getConstructionTime(unitCost, body);

      if (first) {
        finishAt = body.getShipyardStartAt().toInstant().getEpochSecond();
        first = false;
      }
      if (constructionTime >= 1000) {
        assert constructionTime % 1000 == 0;
        var secs = constructionTime / 1000;
        finishAt += count * secs;
      } else {
        assert constructionTime >= 1;
        var numPerSec = 1000 / constructionTime;
        finishAt += (count + numPerSec - 1) / numPerSec;
      }

      ret.add(new ShipyardQueueEntryDto(Converter.convert(kind), count, Converter.convert(totalCost),
          Date.from(Instant.ofEpochSecond(finishAt))));

      // Update the state.
      state.put(kind, state.getOrDefault(kind, 0) + count);
    }

    return ret;
  }

  private List<UnitDto> getUnits(EnumMap<UnitKind, Integer> state, Body body, ProductionDto production,
                                 UnitTypeDto type) {
    Map<UnitKind, UnitItem> items;
    if (type == null) {
      items = UnitItem.getAll();
    } else if (type == UnitTypeDto.DEFENSE) {
      items = UnitItem.getDefense();
    } else {
      assert type == UnitTypeDto.FLEET;
      items = UnitItem.getFleet();
    }

    var resources = body.getResources();

    var units = new ArrayList<UnitDto>(items.size());
    for (var entry : items.entrySet()) {
      var kind = entry.getKey();
      var item = entry.getValue();

      var currentCount = body.getUnitsCount(kind);
      var futureCount = currentCount + state.getOrDefault(kind, 0);

      var meetsRequirements = ItemRequirementsUtils.meetsRequirements(item, body);

      // Don't show the unit if there is no unit on the body and requirements are not met.
      if (futureCount == 0 && !meetsRequirements) {
        continue;
      }

      var cost = item.getCost();
      var time = getConstructionTime(cost, body);

      var missingResources = new Resources(cost);
      missingResources.sub(body.getResources());
      missingResources.max(0.0);
      var neededSmallCargoes = ItemUtils.calcNumUnitsForCapacity(UnitKind.SMALL_CARGO, missingResources);
      var neededLargeCargoes = ItemUtils.calcNumUnitsForCapacity(UnitKind.LARGE_CARGO, missingResources);
      var accumulationTime = ItemTimeUtils.calcAccumulationTime(body.getUpdatedAt(), missingResources, production);

      var maxBuildable = 0;
      if (meetsRequirements) {
        maxBuildable = Integer.MAX_VALUE;

        if (cost.getMetal() > 0.0) {
          maxBuildable = (int) (resources.getMetal() / cost.getMetal());
        }
        if (cost.getCrystal() > 0.0) {
          maxBuildable = Math.min(maxBuildable, (int) (resources.getCrystal() / cost.getCrystal()));
        }
        if (cost.getDeuterium() > 0.0) {
          maxBuildable = Math.min(maxBuildable, (int) (resources.getDeuterium() / cost.getDeuterium()));
        }

        if (kind == UnitKind.SMALL_SHIELD_DOME || kind == UnitKind.LARGE_SHIELD_DOME) {
          var max = calcMaxDomes(state, body, kind);
          maxBuildable = Math.min(maxBuildable, max);
        } else if (kind == UnitKind.ANTI_BALLISTIC_MISSILE || kind == UnitKind.INTERPLANETARY_MISSILE) {
          var max = calcMaxMissiles(state, body, kind);
          maxBuildable = Math.min(maxBuildable, max);
        }
      }

      var unit = new UnitDto(Converter.convert(kind), currentCount, futureCount, Converter.convert(cost), time,
          Converter.convert(missingResources), neededSmallCargoes, neededLargeCargoes, accumulationTime, maxBuildable);
      units.add(unit);
    }

    return units;
  }

  private static int calcMaxDomes(Map<UnitKind, Integer> state, Body body, UnitKind kind) {
    var count = body.getUnitsCount(kind) + state.getOrDefault(kind, 0);
    return count == 0 ? 1 : 0;
  }

  private static int calcMaxMissiles(Map<UnitKind, Integer> state, Body body, UnitKind kind) {
    var capacity = body.getBuildingLevel(BuildingKind.MISSILE_SILO) * 100;
    var numABM =
        body.getUnitsCount(UnitKind.ANTI_BALLISTIC_MISSILE) + state.getOrDefault(UnitKind.ANTI_BALLISTIC_MISSILE, 0);
    var numIPM =
        body.getUnitsCount(UnitKind.INTERPLANETARY_MISSILE) + state.getOrDefault(UnitKind.INTERPLANETARY_MISSILE, 0);
    var max = capacity - (numABM + 2 * numIPM);
    assert max >= 0;
    if (kind == UnitKind.INTERPLANETARY_MISSILE) {
      max /= 2;
    }
    return max;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<UnitKind, Tuple2<Integer, Integer>> getCurrentAndFutureCounts(Body body) {
    EnumMap<UnitKind, Integer> inQueue = body.getShipyardQueue().stream()
        .collect(Collectors.toMap(
            ShipyardQueueEntry::kind,
            ShipyardQueueEntry::count,
            Integer::sum,
            () -> new EnumMap<>(UnitKind.class)
        ));
    return Arrays.stream(UnitKind.values())
        .filter(kind -> body.getUnitsCount(kind) != 0 || inQueue.getOrDefault(kind, 0) != 0)
        .collect(Collectors.toMap(
            Function.identity(),
            kind -> {
              int n = body.getUnitsCount(kind);
              return Tuple.of(n, n + inQueue.getOrDefault(kind, 0));
            },
            (a, b) -> {
              throw new IllegalStateException();
            },
            () -> new EnumMap<>(UnitKind.class)
        ));
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void build(long bodyId, UnitKindDto kind, int count) {
    UnitKind k = Converter.convert(kind);

    Body body = bodyServiceInternal.getUpdated(bodyId);

    var item = Item.get(k);
    if (!ItemRequirementsUtils.meetsRequirements(item, body)) {
      logger.info("Constructing unit failed, requirements not met: bodyId={} kind={} count={}", bodyId, k, count);
      throw new RequirementsNotMetException();
    }

    Resources cost = item.getCost();
    cost.mul(count);
    if (!body.getResources().greaterOrEqual(cost)) {
      logger.info("Constructing unit failed, not enough resources: bodyId={} kind={} count={}", bodyId, k, count);
      throw new NotEnoughResourcesException();
    }
    body.getResources().sub(cost);

    List<ShipyardQueueEntry> queue = body.getShipyardQueue();

    if (k == UnitKind.SMALL_SHIELD_DOME || k == UnitKind.LARGE_SHIELD_DOME) {
      // Special case for shield domes.
      if (count > 1) {
        logger.info("Constructing unit failed, request to build more than one shield dome: bodyId={} kind={} count={}",
            bodyId, k, count);
        throw new TooManyShieldDomesException();
      }
      if (body.getUnitsCount(k) >= 1) {
        logger.info("Constructing unit failed, the shield dome is already built: bodyId={} kind={} count={}",
            bodyId, k, count);
        throw new ShieldDomeAlreadyBuiltException();
      }
      boolean exists = queue.stream().anyMatch(e -> e.kind() == k);
      if (exists) {
        logger.info("Constructing unit failed, the shield dome is already in the queue: bodyId={} kind={} count={}",
            bodyId, k, count);
        throw new ShieldDomeAlreadyInQueueException();
      }
    } else if (k == UnitKind.ANTI_BALLISTIC_MISSILE || k == UnitKind.INTERPLANETARY_MISSILE) {
      // Special case for missiles, check the capacity.
      int used = body.getUnitsCount(UnitKind.ANTI_BALLISTIC_MISSILE) +
          2 * body.getUnitsCount(UnitKind.INTERPLANETARY_MISSILE);
      used += queue.stream()
          .mapToInt(e -> switch (e.kind()) {
            case ANTI_BALLISTIC_MISSILE -> e.count();
            case INTERPLANETARY_MISSILE -> e.count() * 2;
            default -> 0;
          })
          .sum();
      used += (k == UnitKind.ANTI_BALLISTIC_MISSILE ? 1 : 2) * count;
      int cap = 10 * body.getBuildingLevel(BuildingKind.MISSILE_SILO);
      if (used > cap) {
        logger.info("Constructing unit failed, not enough capacity in missile silo: bodyId={} kind={} count={}",
            bodyId, k, count);
        throw new NotEnoughCapacityException();
      }
    }

    logger.info("Constructing unit successful: bodyId={} kind={} count={}", bodyId, k, count);

    ShipyardQueueEntry last = null;
    if (!queue.isEmpty()) {
      last = queue.get(queue.size() - 1);
    }

    // Update or add an entry.
    if (last != null && last.kind() == k) {
      // Add units to last entry.
      var entry = new ShipyardQueueEntry(k, last.count() + count);
      queue.set(queue.size() - 1, entry);
    } else {
      var entry = new ShipyardQueueEntry(k, count);
      queue.add(entry);
    }
    body.setShipyardQueue(queue);

    if (last == null) {
      body.setShipyardStartAt(body.getUpdatedAt());
    }
  }

  public void update(Body body, Date at) {
    if (body.getShipyardStartAt() == null)
      return;

    var startTime = body.getShipyardStartAt().toInstant().getEpochSecond();
    var endTime = at.toInstant().getEpochSecond();
    if (endTime <= startTime)
      return;

    var initialBudget = endTime - startTime;
    var budget = initialBudget;

    var queue = body.getShipyardQueue();
    var it = queue.iterator();
    var changed = false;
    while (it.hasNext()) {
      var entry = it.next();

      var item = Item.get(entry.kind());
      var itemTime = getConstructionTime(item.getCost(), body);
      long maxBuilt;
      if (itemTime >= 1000) {
        assert itemTime % 1000 == 0;
        var secs = itemTime / 1000;
        maxBuilt = budget / secs;
      } else {
        assert itemTime >= 1;
        var numPerSec = 1000 / itemTime;
        maxBuilt = budget * numPerSec;
      }
      maxBuilt = Math.min(Integer.MAX_VALUE, maxBuilt);

      var numBuilt = Math.min(entry.count(), (int) maxBuilt);
      assert numBuilt >= 0;
      if (numBuilt == 0)
        break;

      logger.info("Shipyard: bodyId={} kind={} count={}", body.getId(), entry.kind(), numBuilt);

      changed = true;
      if (itemTime >= 1000) {
        var secs = itemTime / 1000;
        budget -= numBuilt * secs;
      } else {
        var numPerSec = 1000 / itemTime;
        budget -= (numBuilt + numPerSec - 1) / numPerSec;
      }
      assert budget >= 0;
      body.setUnitsCount(entry.kind(), body.getUnitsCount(entry.kind()) + numBuilt);

      var toBuilt = entry.count() - numBuilt;
      if (toBuilt >= 1) {
        var newEntry = new ShipyardQueueEntry(entry.kind(), toBuilt);
        queue.set(0, newEntry);
        break;
      }

      it.remove();
    }

    if (changed) {
      body.setShipyardQueue(queue);
      if (queue.isEmpty()) {
        body.setShipyardStartAt(null);
      } else {
        var spent = initialBudget - budget;
        var newStartAt = startTime + spent;
        assert newStartAt <= endTime;
        body.setShipyardStartAt(Date.from(Instant.ofEpochSecond(newStartAt)));
      }
    }
  }

  private long getConstructionTime(Resources cost, Body body) {
    int shipyardLevel = body.getBuildingLevel(BuildingKind.SHIPYARD);
    int naniteFactoryLevel = body.getBuildingLevel(BuildingKind.NANITE_FACTORY);
    return itemTimeUtils.getUnitConstructionTime(cost, shipyardLevel, naniteFactoryLevel);
  }
}
