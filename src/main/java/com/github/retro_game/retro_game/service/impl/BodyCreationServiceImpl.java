package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.cache.CacheObserver;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.BodyCreationService;
import com.github.retro_game.retro_game.service.exception.BodyExistsException;
import com.github.retro_game.retro_game.service.exception.HomeworldExistsException;
import com.github.retro_game.retro_game.service.exception.NoMoreFreeSlotsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Service
public class BodyCreationServiceImpl implements BodyCreationService {
  private static final Logger logger = LoggerFactory.getLogger(BodyCreationServiceImpl.class);
  private final int homeworldDiameter;
  private final BodyRepository bodyRepository;
  private final UserRepository userRepository;
  private final CacheObserver cacheObserver;

  public BodyCreationServiceImpl(@Value("${retro-game.homeworld-diameter}") int homeworldDiameter,
                                 BodyRepository bodyRepository,
                                 UserRepository userRepository,
                                 CacheObserver cacheObserver) {
    this.homeworldDiameter = homeworldDiameter;
    this.bodyRepository = bodyRepository;
    this.userRepository = userRepository;
    this.cacheObserver = cacheObserver;
  }

  @PostConstruct
  private void checkProperties() {
    Assert.isTrue(homeworldDiameter > 0, "retro-game.homeworld-diameter must be greater than 0");
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public Body createHomeworld(int galaxy, int system, int position) {
    var userId = CustomUser.getCurrentUserId();
    var user = userRepository.getById(userId);

    if (!user.getBodies().isEmpty()) {
      logger.warn("Creating homeworld failed, homeworld exists: userId={}", userId);
      throw new HomeworldExistsException();
    }

    var coordinates = new Coordinates(galaxy, system, position, CoordinatesKind.PLANET);

    if (bodyRepository.existsByCoordinates(coordinates)) {
      logger.warn("Creating homeworld failed, body exists: userId={}, coordinates={}", userId, coordinates);
      throw new BodyExistsException();
    }

    logger.info("Creating homeworld: userId={}, coordinates={}", userId, coordinates);
    var now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    return createPlanet(user, coordinates, "Homeworld", now, homeworldDiameter);
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public Body createHomeworldAtRandomCoordinates() {
    var tries = List.of(
        List.of(4, 6, 8, 10, 12),
        List.of(5, 7, 9, 11),
        IntStream.rangeClosed(1, 15).boxed().toList()
    );

    for (var positions : tries) {
      var coordsOpt = bodyRepository.findFirstFreeHomeworldSlot(5, 500, positions);
      if (coordsOpt.isPresent()) {
        var coords = coordsOpt.get();
        return createHomeworld(coords.getGalaxy(), coords.getSystem(), coords.getPosition());
      }
    }

    logger.warn("Creating homeworld at random coordinates failed, no more free slots");
    throw new NoMoreFreeSlotsException();
  }

  @Override
  public Body createColony(User user, Coordinates coordinates, Date at) {
    var diameter = generatePlanetDiameter(coordinates.getPosition());
    return createPlanet(user, coordinates, "Colony", at, diameter);
  }

  private Body createPlanet(User user, Coordinates coordinates, String name, Date at, int diameter) {
    assert coordinates.getKind() == CoordinatesKind.PLANET;
    var position = coordinates.getPosition();
    var temperature = generateTemperature(position);
    var type = generatePlanetType(position);
    var image = ThreadLocalRandom.current().nextInt(1, 11);
    var resources = new Resources(1000.0, 500.0, 0.0);
    return createBody(user, coordinates, name, at, diameter, temperature, type, image, resources);
  }

  @Override
  public Body createMoon(User user, Coordinates coordinates, Date at, double chance) {
    assert coordinates.getKind() == CoordinatesKind.MOON;
    var diameter = generateMoonDiameter(chance);
    var temperature = generateTemperature(coordinates.getPosition());
    var resources = new Resources();
    return createBody(user, coordinates, "Moon", at, diameter, temperature, BodyType.MOON, 1, resources);
  }

  private Body createBody(User user, Coordinates coordinates, String name, Date at, int diameter, int temperature,
                          BodyType type, int image, Resources resources) {
    var body = new Body();
    body.setUser(user);
    body.setCoordinates(coordinates);
    body.setName(name);
    body.setCreatedAt(at);
    body.setUpdatedAt(at);
    body.setDiameter(diameter);
    body.setTemperature(temperature);
    body.setType(type);
    body.setImage(image);
    body.setResources(resources);
    body.setProductionFactors(new ProductionFactors());
    body.setLastJumpAt(at);
    body.setBuildings(Collections.emptyMap());
    body.setUnits(Collections.emptyMap());
    body.setBuildingQueue(Collections.emptySortedMap());
    body.setShipyardQueue(Collections.emptyList());
    body = bodyRepository.save(body);

    cacheObserver.notifyBodyCreated(user.getId());

    return body;
  }

  private BodyType generatePlanetType(int position) {
    return switch (position) {
      case 1, 2, 3 -> ThreadLocalRandom.current().nextInt(0, 2) == 0 ? BodyType.DRY : BodyType.DESERT;
      case 4, 5, 6 -> BodyType.JUNGLE;
      case 7, 8 -> BodyType.NORMAL;
      case 9 -> ThreadLocalRandom.current().nextInt(0, 2) == 0 ? BodyType.NORMAL : BodyType.WATER;
      case 10, 11, 12 -> BodyType.WATER;
      case 13 -> BodyType.ICE;
      case 14, 15 -> ThreadLocalRandom.current().nextInt(0, 2) == 0 ? BodyType.ICE : BodyType.GAS;
      default -> throw new IllegalArgumentException();
    };
  }

  private int generatePlanetDiameter(int position) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    double x = Math.abs(8 - position);
    double mean = 200.0 - 10.0 * x;
    double sd = 60.0 - 5.0 * x;
    double numFields = mean + 0.33333 * sd * random.nextGaussian();
    numFields = Math.max(numFields, 42.0);
    return (int) (Math.sqrt(numFields) * 100.0) * 10;
  }

  private int generateMoonDiameter(double chance) {
    chance = Math.max(0.01, Math.min(0.2, chance));
    int r = ThreadLocalRandom.current().nextInt(10, 20 + 1);
    return (int) (1000.0 * Math.sqrt(r + 3 * (int) (100.0 * chance)));
  }

  private int generateTemperature(int position) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    double x = 8 - position;
    double mean = 30.0 + 1.75 * Math.signum(x) * x * x;
    int temperature = (int) (mean + 10.0 * random.nextGaussian());
    return Math.max(-60, Math.min(120, temperature));
  }
}
