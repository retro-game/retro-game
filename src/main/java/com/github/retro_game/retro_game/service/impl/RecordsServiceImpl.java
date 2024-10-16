package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.cache.UserInfoCache;
import com.github.retro_game.retro_game.dto.RecordDto;
import com.github.retro_game.retro_game.dto.ResourcesDto;
import com.github.retro_game.retro_game.entity.Record;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.repository.FlightRepository;
import com.github.retro_game.retro_game.repository.RecordRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.RecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecordsServiceImpl implements RecordsService {
  private static final int MAX_HOLDERS = 3;
  private final UserInfoCache userInfoCache;
  private final FlightRepository flightRepository;
  private final RecordRepository recordRepository;
  private final UserRepository userRepository;
  private BodyServiceInternal bodyServiceInternal;

  public RecordsServiceImpl(UserInfoCache userInfoCache, FlightRepository flightRepository,
                            RecordRepository recordRepository, UserRepository userRepository) {
    this.userInfoCache = userInfoCache;
    this.flightRepository = flightRepository;
    this.recordRepository = recordRepository;
    this.userRepository = userRepository;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Override
  public Map<String, RecordDto> getRecords() {
    var records = recordRepository.findAll();

    var userNames = records.stream()
        .map(Record::getHolders)
        .flatMapToLong(Arrays::stream)
        .boxed()
        .collect(Collectors.toSet())
        .stream()
        .collect(Collectors.toMap(Function.identity(), id -> userInfoCache.get(id).getName()));

    var now = Instant.now().getEpochSecond();
    var newThreshold = now - 24 * 60 * 60;

    return records.stream().collect(Collectors.toMap(
        Record::getKey,
        record -> {
          var holders = Arrays.stream(record.getHolders()).mapToObj(userNames::get).toList();
          var at = record.getAt().toInstant().getEpochSecond();
          var isNew = at >= newThreshold;
          return new RecordDto(record.getValue(), record.getAt(), holders, isNew);
        }
    ));
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void share(long bodyId, boolean buildings, boolean technologies, boolean units, boolean production,
                    boolean other) {
    if (!buildings && !technologies && !units && !other) return;

    var userId = CustomUser.getCurrentUserId();
    var user = userRepository.getById(userId);
    var bodies = user.getBodies();

    // Update resources and shipyard on all bodies.
    var now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    for (var entry : bodies.entrySet())
      bodyServiceInternal.updateResourcesAndShipyard(entry.getValue(), now);

    var records = recordRepository.findAll().stream().collect(Collectors.toMap(Record::getKey, Function.identity()));

    if (buildings) {
      for (var kind : BuildingKind.values()) {
        var max = bodies.values().stream().mapToInt(b -> b.getBuildingLevel(kind)).max().orElse(0);
        if (max == 0) continue;
        updateRecords(records, now, "BUILDING_" + kind, max, userId);
      }
    }

    if (technologies) {
      for (var kind : TechnologyKind.values()) {
        var level = user.getTechnologyLevel(kind);
        if (level == 0) continue;
        updateRecords(records, now, "TECHNOLOGY_" + kind, level, userId);
      }
    }

    if (units) {
      var flights = flightRepository.findByStartUser(user);
      for (var kind : UnitKind.values()) {
        var sum = bodies.values().stream().mapToLong(b -> b.getUnitsCount(kind)).sum();
        if (UnitItem.getFleet().containsKey(kind))
          sum += flights.stream().mapToLong(f -> f.getUnitsCount(kind)).sum();
        if (sum == 0) continue;
        updateRecords(records, now, "UNIT_" + kind, sum, userId);
      }
    }

    if (production) {
      var prod = bodies.values().stream()
          .map(b -> bodyServiceInternal.getProduction(b))
          .map(p -> new ResourcesDto(p.metalProduction(), p.crystalProduction(), p.deuteriumProduction()))
          .reduce(
              new ResourcesDto(0, 0, 0),
              (lhs, rhs) -> new ResourcesDto(
                  lhs.getMetal() + rhs.getMetal(),
                  lhs.getCrystal() + rhs.getCrystal(),
                  lhs.getDeuterium() + rhs.getDeuterium()
              )
          );
      updateRecords(records, now, "PRODUCTION_METAL", (long) prod.getMetal(), userId);
      updateRecords(records, now, "PRODUCTION_CRYSTAL", (long) prod.getCrystal(), userId);
      updateRecords(records, now, "PRODUCTION_DEUTERIUM", (long) prod.getDeuterium(), userId);
    }

    if (other) {
      // Number of bodies.
      Function<CoordinatesKind, Long> numBodies = (CoordinatesKind kind) -> bodies.values().stream()
          .filter(b -> b.getCoordinates().getKind() == kind)
          .count();
      long numPlanets = numBodies.apply(CoordinatesKind.PLANET);
      long numMoons = numBodies.apply(CoordinatesKind.MOON);
      if (numPlanets > 0) updateRecords(records, now, "NUM_PLANETS", numPlanets, userId);
      if (numMoons > 0) updateRecords(records, now, "NUM_MOONS", numMoons, userId);

      // Diameter.
      Function<CoordinatesKind, Integer> maxDiameter = (CoordinatesKind kind) -> bodies.values().stream()
          .filter(b -> b.getCoordinates().getKind() == kind)
          .mapToInt(Body::getDiameter)
          .max()
          .orElse(0);
      int planetDiameter = maxDiameter.apply(CoordinatesKind.PLANET);
      int moonDiameter = maxDiameter.apply(CoordinatesKind.MOON);
      if (planetDiameter > 0) updateRecords(records, now, "PLANET_DIAMETER", planetDiameter, userId);
      if (moonDiameter > 0) updateRecords(records, now, "MOON_DIAMETER", moonDiameter, userId);
    }

    recordRepository.saveAll(records.values());
  }

  private static void updateRecords(Map<String, Record> records, Date now, String key, long newValue, long userId) {
    var record = records.get(key);

    var noRecord = record == null;
    if (noRecord) {
      record = new Record();
      record.setKey(key);
      records.put(key, record);
    }

    // New record?
    if (noRecord || record.getValue() < newValue) {
      record.setValue(newValue);
      record.setAt(now);
      record.setHolders(new long[]{userId});
      return;
    }

    // New holder?
    var holders = record.getHolders();
    if (record.getValue() == newValue && holders.length < MAX_HOLDERS &&
        Arrays.stream(holders).noneMatch(id -> id == userId)) {
      var newHolders = new long[holders.length + 1];
      System.arraycopy(holders, 0, newHolders, 0, holders.length);
      newHolders[holders.length] = userId;
      record.setHolders(newHolders);
    }
  }
}
