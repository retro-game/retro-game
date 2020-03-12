package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.dto.*;
import com.github.retro_game.retro_game.service.exception.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service("bodyService")
class BodyServiceImpl implements BodyServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(BodyServiceImpl.class);
  private final int homeworldDiameter;
  private final int productionSpeed;
  private final int metalBaseProduction;
  private final int crystalBaseProduction;
  private final int deuteriumBaseProduction;
  private final int metalMineBaseProduction;
  private final int crystalMineBaseProduction;
  private final int deuteriumSynthesizerBaseProduction;
  private final int metalMineBaseEnergyUsage;
  private final int crystalMineBaseEnergyUsage;
  private final int deuteriumSynthesizerBaseEnergyUsage;
  private final int solarPlantBaseEnergyProduction;
  private final int fusionReactorBaseEnergyProduction;
  private final int fusionReactorBaseDeuteriumUsage;
  private final int fieldsPerTerraformerLevel;
  private final int fieldsPerLunarBaseLevel;
  private final BodyRepository bodyRepository;
  private final UserRepository userRepository;
  private BuildingsServiceInternal buildingsServiceInternal;
  private FlightServiceInternal flightServiceInternal;
  private ShipyardServiceInternal shipyardServiceInternal;
  private UserServiceInternal userServiceInternal;

  public BodyServiceImpl(@Value("${retro-game.homeworld-diameter}") int homeworldDiameter,
                         @Value("${retro-game.production-speed}") int productionSpeed,
                         @Value("${retro-game.metal-base-production}") int metalBaseProduction,
                         @Value("${retro-game.crystal-base-production}") int crystalBaseProduction,
                         @Value("${retro-game.deuterium-base-production}") int deuteriumBaseProduction,
                         @Value("${retro-game.metal-mine-base-production}") int metalMineBaseProduction,
                         @Value("${retro-game.crystal-mine-base-production}") int crystalMineBaseProduction,
                         @Value("${retro-game.deuterium-synthesizer-base-production}") int deuteriumSynthesizerBaseProduction,
                         @Value("${retro-game.metal-mine-base-energy-usage}") int metalMineBaseEnergyUsage,
                         @Value("${retro-game.crystal-mine-base-energy-usage}") int crystalMineBaseEnergyUsage,
                         @Value("${retro-game.deuterium-synthesizer-base-energy-usage}") int deuteriumSynthesizerBaseEnergyUsage,
                         @Value("${retro-game.solar-plant-base-energy-production}") int solarPlantBaseEnergyProduction,
                         @Value("${retro-game.fusion-reactor-base-energy-production}") int fusionReactorBaseEnergyProduction,
                         @Value("${retro-game.fusion-reactor-base-deuterium-usage}") int fusionReactorBaseDeuteriumUsage,
                         @Value("${retro-game.fields-per-terraformer-level}") int fieldsPerTerraformerLevel,
                         @Value("${retro-game.fields-per-lunar-base-level}") int fieldsPerLunarBaseLevel,
                         BodyRepository bodyRepository,
                         UserRepository userRepository) {
    this.homeworldDiameter = homeworldDiameter;
    this.productionSpeed = productionSpeed;
    this.metalBaseProduction = metalBaseProduction;
    this.crystalBaseProduction = crystalBaseProduction;
    this.deuteriumBaseProduction = deuteriumBaseProduction;
    this.metalMineBaseProduction = metalMineBaseProduction;
    this.crystalMineBaseProduction = crystalMineBaseProduction;
    this.deuteriumSynthesizerBaseProduction = deuteriumSynthesizerBaseProduction;
    this.metalMineBaseEnergyUsage = metalMineBaseEnergyUsage;
    this.crystalMineBaseEnergyUsage = crystalMineBaseEnergyUsage;
    this.deuteriumSynthesizerBaseEnergyUsage = deuteriumSynthesizerBaseEnergyUsage;
    this.solarPlantBaseEnergyProduction = solarPlantBaseEnergyProduction;
    this.fusionReactorBaseEnergyProduction = fusionReactorBaseEnergyProduction;
    this.fusionReactorBaseDeuteriumUsage = fusionReactorBaseDeuteriumUsage;
    this.fieldsPerTerraformerLevel = fieldsPerTerraformerLevel;
    this.fieldsPerLunarBaseLevel = fieldsPerLunarBaseLevel;
    this.bodyRepository = bodyRepository;
    this.userRepository = userRepository;
  }

  @Autowired
  public void setBuildingsServiceInternal(BuildingsServiceInternal buildingsServiceInternal) {
    this.buildingsServiceInternal = buildingsServiceInternal;
  }

  @Autowired
  public void setFlightServiceInternal(FlightServiceInternal flightServiceInternal) {
    this.flightServiceInternal = flightServiceInternal;
  }

  @Autowired
  public void setShipyardServiceInternal(ShipyardServiceInternal shipyardServiceInternal) {
    this.shipyardServiceInternal = shipyardServiceInternal;
  }

  @Autowired
  public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
    this.userServiceInternal = userServiceInternal;
  }

  @PostConstruct
  private void checkProperties() {
    Assert.isTrue(homeworldDiameter > 0,
        "retro-game.homeworld-diameter must be greater than 0");

    Assert.isTrue(productionSpeed >= 1,
        "retro-game.production-speed must be at least 1");

    Assert.isTrue(metalBaseProduction >= 0,
        "retro-game.metal-base-production must be at least 0");
    Assert.isTrue(crystalBaseProduction >= 0,
        "retro-game.crystal-base-production must be at least 0");
    Assert.isTrue(deuteriumBaseProduction >= 0,
        "retro-game.deuterium-base-production must be at least 0");

    Assert.isTrue(metalMineBaseProduction > 0,
        "retro-game.metal-mine-base-production must be greater than 0");
    Assert.isTrue(crystalMineBaseProduction > 0,
        "retro-game.crystal-mine-base-production must be greater than 0");
    Assert.isTrue(deuteriumSynthesizerBaseProduction > 0,
        "retro-game.deuterium-synthesizer-base-production must be greater than 0");

    Assert.isTrue(metalMineBaseEnergyUsage >= 0,
        "retro-game.metal-mine-base-production must be at least 0");
    Assert.isTrue(crystalMineBaseEnergyUsage >= 0,
        "retro-game.crystal-mine-base-production must be at least 0");
    Assert.isTrue(deuteriumSynthesizerBaseEnergyUsage >= 0,
        "retro-game.deuterium-synthesizer-base-production must be at least 0");

    Assert.isTrue(solarPlantBaseEnergyProduction > 0,
        "retro-game.solar-plant-base-energy-production must be greater than 0");
    Assert.isTrue(fusionReactorBaseEnergyProduction > 0,
        "retro-game.fusion-reactor-base-energy-production must be greater than 0");
    Assert.isTrue(fusionReactorBaseDeuteriumUsage >= 0,
        "retro-game.fusion-reactor-base-deuterium-usage must be at least 0");

    Assert.isTrue(fieldsPerTerraformerLevel > 1,
        "retro-game.fields-per-terraformer-level must be greater than 1");
    Assert.isTrue(fieldsPerLunarBaseLevel > 1,
        "retro-game.fields-per-lunar-base-level must be greater than 1");
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public long createHomeworld(int galaxy, int system, int position) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    if (!user.getBodies().isEmpty()) {
      logger.warn("Creating homeworld failed, homeworld exists: userId={}", userId);
      throw new HomeworldExistsException();
    }

    Coordinates coordinates = new Coordinates(galaxy, system, position, CoordinatesKind.PLANET);

    if (bodyRepository.existsByCoordinates(coordinates)) {
      logger.warn("Creating homeworld failed, body exists: userId={}, coordinates={}", userId, coordinates);
      throw new BodyExistsException();
    }

    logger.info("Creating homeworld: userId={}, coordinates={}", userId, coordinates);
    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    Body body = new Body();
    body.setUser(user);
    body.setCoordinates(coordinates);
    body.setName("Homeworld");
    body.setCreatedAt(now);
    body.setUpdatedAt(now);
    body.setDiameter(homeworldDiameter);
    body.setTemperature(generateTemperature(position));
    body.setType(generatePlanetType(coordinates.getPosition()));
    body.setImage(ThreadLocalRandom.current().nextInt(1, 11));
    body.setResources(new Resources(1000.0, 500.0, 0.0));
    body.setProductionFactors(new ProductionFactors());
    body = bodyRepository.save(body);
    return body.getId();
  }

  @Override
  @CacheEvict(cacheNames = "bodiesBasicInfo", key = "#user.id")
  public Body createColony(User user, Coordinates coordinates, Date at) {
    assert coordinates.getKind() == CoordinatesKind.PLANET;
    Body body = new Body();
    body.setUser(user);
    body.setCoordinates(coordinates);
    body.setName("Colony");
    body.setCreatedAt(at);
    body.setUpdatedAt(at);
    body.setDiameter(generatePlanetDiameter(coordinates.getPosition()));
    body.setTemperature(generateTemperature(coordinates.getPosition()));
    body.setType(generatePlanetType(coordinates.getPosition()));
    body.setImage(ThreadLocalRandom.current().nextInt(1, 11));
    body.setResources(new Resources(1000.0, 500.0, 0.0));
    body.setProductionFactors(new ProductionFactors());
    return bodyRepository.save(body);
  }

  @Override
  @CacheEvict(cacheNames = "bodiesBasicInfo", key = "#user.id")
  public Body createMoon(User user, Coordinates coordinates, Date at, double chance) {
    assert coordinates.getKind() == CoordinatesKind.MOON;
    assert chance >= 0.01 && chance <= 0.2;
    Body body = new Body();
    body.setUser(user);
    body.setCoordinates(coordinates);
    body.setName("Moon");
    body.setCreatedAt(at);
    body.setUpdatedAt(at);
    body.setDiameter(generateMoonDiameter(chance));
    body.setTemperature(generateTemperature(coordinates.getPosition()));
    body.setType(BodyType.MOON);
    body.setImage(1);
    body.setResources(new Resources());
    body.setProductionFactors(new ProductionFactors());
    body.setLastJumpAt(at);
    return bodyRepository.save(body);
  }

  private BodyType generatePlanetType(int position) {
    switch (position) {
      case 1:
      case 2:
      case 3:
        return ThreadLocalRandom.current().nextInt(0, 2) == 0 ? BodyType.DRY : BodyType.DESERT;
      case 4:
      case 5:
      case 6:
        return BodyType.JUNGLE;
      case 7:
      case 8:
        return BodyType.NORMAL;
      case 9:
        return ThreadLocalRandom.current().nextInt(0, 2) == 0 ? BodyType.NORMAL : BodyType.WATER;
      case 10:
      case 11:
      case 12:
        return BodyType.WATER;
      case 13:
        return BodyType.ICE;
      case 14:
      case 15:
        return ThreadLocalRandom.current().nextInt(0, 2) == 0 ? BodyType.ICE : BodyType.GAS;
      default:
        throw new IllegalArgumentException();
    }
  }

  private int generatePlanetDiameter(int position) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    double x = Math.abs(8 - position);
    double mean = 200.0 - 10.0 * x;
    double sd = 60.0 - 5.0 * x;
    double numFields = mean + sd * random.nextGaussian();
    numFields = Math.max(numFields, 42.0);
    return (int) (Math.sqrt(numFields) * 100.0) * 10;
  }

  private int generateMoonDiameter(double chance) {
    assert chance >= 0.01 && chance <= 0.2;
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

  @Override
  public int getUsedFields(Body body) {
    return body.getBuildings().values().stream().mapToInt(Building::getLevel).sum();
  }

  @Override
  public int getMaxFields(Body body) {
    if (body.getCoordinates().getKind() == CoordinatesKind.PLANET) {
      int level = body.getBuildingLevel(BuildingKind.TERRAFORMER);
      return getPlanetMaxFields(body.getDiameter(), level);
    } else {
      assert body.getCoordinates().getKind() == CoordinatesKind.MOON;
      int level = body.getBuildingLevel(BuildingKind.LUNAR_BASE);
      return getMoonMaxFields(level);
    }
  }

  @Override
  public int getMaxFields(Body body, Map<BuildingKind, Integer> buildings) {
    if (body.getCoordinates().getKind() == CoordinatesKind.PLANET) {
      int level = buildings.getOrDefault(BuildingKind.TERRAFORMER, 0);
      return getPlanetMaxFields(body.getDiameter(), level);
    } else {
      assert body.getCoordinates().getKind() == CoordinatesKind.MOON;
      int level = buildings.getOrDefault(BuildingKind.LUNAR_BASE, 0);
      return getMoonMaxFields(level);
    }
  }

  private int getPlanetMaxFields(int diameter, int terraformerLevel) {
    assert diameter > 0;
    float x = diameter / 1000.0f;
    return (int) (x * x) + fieldsPerTerraformerLevel * terraformerLevel;
  }

  private int getMoonMaxFields(int lunarBaseLevel) {
    return 1 + fieldsPerLunarBaseLevel * lunarBaseLevel;
  }

  @Override
  public int getTemperature(long bodyId) {
    Body body = bodyRepository.findById(bodyId).orElseThrow(BodyDoesntExistException::new);
    return body.getTemperature();
  }

  @Override
  public BodyBasicInfoDto getBodyBasicInfo(long bodyId) {
    List<BodyBasicInfoDto> bodies = getBodiesBasicInfo(bodyId);
    return bodies.stream().filter(b -> b.getId() == bodyId).findAny().orElseThrow(BodyDoesntExistException::new);
  }

  @Override
  // The cache must be evicted every time the list may change, this includes colonizing, deleting bodies, updating name
  // and also when the sort order of bodies changes (when a user updates hers settings, see UserServiceImpl).
  @Cacheable(cacheNames = "bodiesBasicInfo", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public List<BodyBasicInfoDto> getBodiesBasicInfo(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    List<BodyBasicInfoDto> bodies = user.getBodies().values().stream()
        .map(b -> new BodyBasicInfoDto(b.getId(), b.getName(), Converter.convert(b.getCoordinates())))
        .collect(Collectors.toList());
    BodyKeyExtractors<BodyBasicInfoDto> keyExtractors = new BodyKeyExtractors<>(BodyBasicInfoDto::getId,
        BodyBasicInfoDto::getCoordinates, BodyBasicInfoDto::getName);
    sort(bodies, keyExtractors, user);
    return bodies;
  }

  @Override
  public BodyTypeAndImagePairDto getBodyTypeAndImagePair(long bodyId) {
    Body body = bodyRepository.findById(bodyId).orElseThrow(BodyDoesntExistException::new);
    return new BodyTypeAndImagePairDto(Converter.convert(body.getType()), body.getImage());
  }

  @Override
  public BodiesPointersDto getBodiesPointers(long bodyId) {
    BodyBasicInfoDto prev = null;
    BodyBasicInfoDto next = null;
    Iterator<BodyBasicInfoDto> it = getBodiesBasicInfo(bodyId).iterator();
    assert it.hasNext();
    BodyBasicInfoDto cur = it.next();
    while (cur != null) {
      next = it.hasNext() ? it.next() : null;
      if (cur.getId() == bodyId) {
        break;
      }
      prev = cur;
      cur = next;
    }
    return new BodiesPointersDto(prev, next);
  }

  @Override
  @Transactional(readOnly = true)
  public OverviewBodiesDto getOverviewBodies(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    Map<Long, Body> bodies = user.getBodies();

    OverviewBodyInfoDto selectedInfo;
    Body selected = bodies.get(bodyId);
    {
      BuildingKindDto kind = null;
      int level = 0;
      Optional<OngoingBuildingDto> ongoingBuildingOptional = buildingsServiceInternal.getOngoingBuilding(selected);
      if (ongoingBuildingOptional.isPresent()) {
        OngoingBuildingDto ongoingBuilding = ongoingBuildingOptional.get();
        kind = Converter.convert(ongoingBuilding.getKind());
        level = ongoingBuilding.getLevel();
      }

      Date finishAt = buildingsServiceInternal.getOngoingBuildingFinishAt(selected).orElse(null);

      int usedFields = getUsedFields(selected);
      int maxFields = getMaxFields(selected);

      selectedInfo = new OverviewBodyInfoDto(selected.getId(), Converter.convert(selected.getCoordinates()),
          selected.getName(), selected.getDiameter(), selected.getTemperature(), Converter.convert(selected.getType()),
          selected.getImage(), kind, level, finishAt, usedFields, maxFields);
    }

    // Find associated body, i.e. planet / moon with the same coordinates except for the kind, which is typically
    // displayed on the left side in overview.
    Coordinates selectedCoords = selected.getCoordinates();
    Optional<Body> associatedOptional = bodies.values().stream()
        .filter(b -> {
          Coordinates coords = b.getCoordinates();
          return coords.getGalaxy() == selectedCoords.getGalaxy() &&
              coords.getSystem() == selectedCoords.getSystem() &&
              coords.getPosition() == selectedCoords.getPosition() &&
              coords.getKind() != selectedCoords.getKind();
        })
        .findFirst();

    // All other planets.
    List<Body> otherBodies = bodies.entrySet().stream()
        .filter(entry -> entry.getKey() != bodyId &&
            (!associatedOptional.isPresent() || entry.getKey() != associatedOptional.get().getId()) &&
            entry.getValue().getCoordinates().getKind() == CoordinatesKind.PLANET)
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    // Put associated body into otherBodies, so that we can generate required information without code duplication.
    associatedOptional.ifPresent(otherBodies::add);

    // Generate info for other planets and the associated body (if exists).
    List<OverviewBodyBasicInfoDto> basicInfo = new ArrayList<>(otherBodies.size());
    for (Body body : otherBodies) {
      BuildingKindDto kind = null;
      int level = 0;
      Optional<OngoingBuildingDto> ongoingBuildingOptional = buildingsServiceInternal.getOngoingBuilding(body);
      if (ongoingBuildingOptional.isPresent()) {
        OngoingBuildingDto ongoingBuilding = ongoingBuildingOptional.get();
        kind = Converter.convert(ongoingBuilding.getKind());
        level = ongoingBuilding.getLevel();
      }

      basicInfo.add(new OverviewBodyBasicInfoDto(body.getId(), Converter.convert(body.getCoordinates()), body.getName(),
          Converter.convert(body.getType()), body.getImage(), kind, level));
    }

    // The associated body info is at the end, remove it from the rest.
    OverviewBodyBasicInfoDto associatedInfo =
        associatedOptional.isPresent() ? basicInfo.remove(basicInfo.size() - 1) : null;

    // Sort other bodies.
    BodyKeyExtractors<OverviewBodyBasicInfoDto> keyExtractors = new BodyKeyExtractors<>(OverviewBodyBasicInfoDto::getId,
        OverviewBodyBasicInfoDto::getCoordinates, OverviewBodyBasicInfoDto::getName);
    sort(basicInfo, keyExtractors, user);

    return new OverviewBodiesDto(selectedInfo, associatedInfo, basicInfo);
  }

  @Override
  @Transactional(readOnly = true)
  public EmpireDto getEmpire(long bodyId, @Nullable Integer galaxy, @Nullable Integer system,
                             @Nullable Integer position, @Nullable CoordinatesKindDto kind) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    CoordinatesKind k = kind != null ? Converter.convert(kind) : null;

    List<Body> bodies = bodyRepository.findByUserForEmpire(user, galaxy, system, position, k);

    // Selected bodies.
    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    List<EmpireBodyDto> empireBodies = bodies.stream()
        .map(body -> {
          updateResources(body, now);

          int usedFields = getUsedFields(body);
          int maxFields = getMaxFields(body);

          ResourcesDto res = Converter.convert(body.getResources());
          long totalRes = (long) (res.getMetal() + res.getCrystal() + res.getDeuterium());
          Tuple2<ResourcesDto, Long> resources = Tuple.of(res, totalRes);

          ProductionDto production = getProduction(body);

          int availableEnergy = production.getAvailableEnergy();
          int totalEnergy = production.getTotalEnergy();

          double m = production.getMetalProduction();
          double c = production.getCrystalProduction();
          double d = production.getDeuteriumProduction();

          ResourcesDto hourly = new ResourcesDto(m, c, d);
          long hourlyTotal = (long) (m + c + d);
          Tuple2<ResourcesDto, Long> productionHourly = Tuple.of(hourly, hourlyTotal);

          ResourcesDto daily = new ResourcesDto(24 * m, 24 * c, 24 * d);
          long dailyTotal = 24 * (long) (m + c + d);
          Tuple2<ResourcesDto, Long> productionDaily = Tuple.of(daily, dailyTotal);

          ResourcesDto weekly = new ResourcesDto(24 * 7 * m, 24 * 7 * c, 24 * 7 * d);
          long weeklyTotal = 24 * 7 * (long) (m + c + d);
          Tuple2<ResourcesDto, Long> productionWeekly = Tuple.of(weekly, weeklyTotal);

          ResourcesDto _30days = new ResourcesDto(24 * 30 * m, 24 * 30 * c, 24 * 30 * d);
          long _30daysTotal = 24 * 30 * (long) (m + c + d);
          Tuple2<ResourcesDto, Long> production30days = Tuple.of(_30days, _30daysTotal);

          ResourcesDto cap = getCapacity(body);
          long totalCap = (long) (cap.getMetal() + cap.getCrystal() + cap.getDeuterium());
          Tuple2<ResourcesDto, Long> capacity = Tuple.of(cap, totalCap);

          Map<BuildingKindDto, Tuple2<Integer, Integer>> buildings = Converter.convertToEnumMap(
              buildingsServiceInternal.getCurrentAndFutureLevels(body), BuildingKindDto.class, Converter::convert,
              Function.identity());

          Map<UnitKindDto, Tuple2<Integer, Integer>> units = Converter.convertToEnumMap(
              shipyardServiceInternal.getCurrentAndFutureCounts(body), UnitKindDto.class, Converter::convert,
              Function.identity());

          return new EmpireBodyDto(body.getId(), body.getName(), Converter.convert(body.getCoordinates()),
              Converter.convert(body.getType()), body.getImage(), body.getDiameter(), usedFields, maxFields,
              body.getTemperature(), resources, availableEnergy, totalEnergy, productionHourly, productionDaily,
              productionWeekly, production30days, capacity, buildings, units);
        })
        .collect(Collectors.toList());

    // Total.
    Supplier<Tuple2<ResourcesDto, Long>> resTuplesId = () -> Tuple.of(new ResourcesDto(0.0, 0.0, 0.0), 0L);
    BinaryOperator<Tuple2<ResourcesDto, Long>> resTuplesAcc = (acc, elem) -> {
      ResourcesDto a = acc._1;
      ResourcesDto b = elem._1;
      ResourcesDto r = new ResourcesDto(
          a.getMetal() + b.getMetal(),
          a.getCrystal() + b.getCrystal(),
          a.getDeuterium() + b.getDeuterium());
      return Tuple.of(r, acc._2 + elem._2);
    };
    EmpireSummaryDto<Long> total = new EmpireSummaryDto<>(
        empireBodies.stream().mapToLong(EmpireBodyDto::getDiameter).sum(),
        empireBodies.stream().mapToLong(EmpireBodyDto::getUsedFields).sum(),
        empireBodies.stream().mapToLong(EmpireBodyDto::getMaxFields).sum(),
        empireBodies.stream().mapToLong(EmpireBodyDto::getTemperature).sum(),
        empireBodies.stream().map(EmpireBodyDto::getResources).reduce(resTuplesId.get(), resTuplesAcc),
        empireBodies.stream().mapToLong(EmpireBodyDto::getAvailableEnergy).sum(),
        empireBodies.stream().mapToLong(EmpireBodyDto::getTotalEnergy).sum(),
        empireBodies.stream().map(EmpireBodyDto::getProductionHourly).reduce(resTuplesId.get(), resTuplesAcc),
        empireBodies.stream().map(EmpireBodyDto::getProductionDaily).reduce(resTuplesId.get(), resTuplesAcc),
        empireBodies.stream().map(EmpireBodyDto::getProductionWeekly).reduce(resTuplesId.get(), resTuplesAcc),
        empireBodies.stream().map(EmpireBodyDto::getProduction30days).reduce(resTuplesId.get(), resTuplesAcc),
        empireBodies.stream().map(EmpireBodyDto::getCapacity).reduce(resTuplesId.get(), resTuplesAcc),
        empireBodies.stream().flatMap(body -> body.getBuildings().entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Tuple.of((long) e.getValue()._1, (long) e.getValue()._2),
                (a, b) -> Tuple.of(a._1 + b._1, a._2 + b._2),
                () -> new EnumMap<>(BuildingKindDto.class)
            )),
        empireBodies.stream().flatMap(body -> body.getUnits().entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Tuple.of((long) e.getValue()._1, (long) e.getValue()._2),
                (a, b) -> Tuple.of(a._1 + b._1, a._2 + b._2),
                () -> new EnumMap<>(UnitKindDto.class)
            )));

    // Average.
    final int size = empireBodies.size();
    assert size > 0;
    Function<Tuple2<ResourcesDto, Long>, Tuple2<ResourcesDto, Double>> resTuplesAvg = (t) -> {
      ResourcesDto r = t._1;
      return Tuple.of(
          new ResourcesDto(r.getMetal() / size, r.getCrystal() / size, r.getDeuterium() / size),
          (double) t._2 / size);
    };
    EmpireSummaryDto<Double> average = new EmpireSummaryDto<>(
        (double) total.getDiameter() / size,
        (double) total.getUsedFields() / size,
        (double) total.getMaxFields() / size,
        (double) total.getTemperature() / size,
        resTuplesAvg.apply(total.getResources()),
        (double) total.getAvailableEnergy() / size,
        (double) total.getTotalEnergy() / size,
        resTuplesAvg.apply(total.getProductionHourly()),
        resTuplesAvg.apply(total.getProductionDaily()),
        resTuplesAvg.apply(total.getProductionWeekly()),
        resTuplesAvg.apply(total.getProduction30days()),
        resTuplesAvg.apply(total.getCapacity()),
        total.getBuildings().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Tuple.of((double) e.getValue()._1 / size, (double) e.getValue()._2 / size),
                (a, b) -> {
                  throw new IllegalStateException();
                },
                () -> new EnumMap<>(BuildingKindDto.class)
            )),
        total.getUnits().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Tuple.of((double) e.getValue()._1 / size, (double) e.getValue()._2 / size),
                (a, b) -> {
                  throw new IllegalStateException();
                },
                () -> new EnumMap<>(UnitKindDto.class)
            )));

    // Sort.
    BodyKeyExtractors<EmpireBodyDto> keyExtractors = new BodyKeyExtractors<>(EmpireBodyDto::getId,
        EmpireBodyDto::getCoordinates, EmpireBodyDto::getName);
    sort(empireBodies, keyExtractors, user);

    return new EmpireDto(empireBodies, total, average);
  }

  private static <T> void sort(List<T> bodies, BodyKeyExtractors<T> extractors, User user) {
    Comparator<T> comparator;
    switch (user.getBodiesSortOrder()) {
      case COORDINATES:
        comparator = Comparator.comparing(extractors.getCoordinatesExtractor());
        break;
      case NAME:
        comparator = Comparator.comparing(extractors.getNameExtractor());
        break;
      default:
        assert user.getBodiesSortOrder() == BodiesSortOrder.EMERGENCE;
        comparator = Comparator.comparing(extractors.getIdExtractor());
    }
    if (user.getBodiesSortDirection() == Sort.Direction.DESC) {
      comparator = comparator.reversed();
    }

    if (!user.hasFlag(UserFlag.STICKY_MOONS)) {
      bodies.sort(comparator);
      return;
    }

    // Sticky moons, i.e. a moon will be always after the associated planet.

    // Partition by kind.
    Function<? super T, ? extends CoordinatesDto> coordinatesExtractor = extractors.getCoordinatesExtractor();
    Map<Boolean, ArrayList<T>> partition = bodies.stream()
        .collect(
            Collectors.partitioningBy(
                b -> coordinatesExtractor.apply(b).getKind() == CoordinatesKindDto.PLANET,
                Collectors.toCollection(ArrayList::new)));
    ArrayList<T> moons = partition.get(false);
    ArrayList<T> planets = partition.get(true);

    // Sort planets using user specified comparator.
    planets.sort(comparator);

    bodies.clear();
    for (T planet : planets) {
      bodies.add(planet);
      // Add the associated moon, if any.
      CoordinatesDto planetCoords = coordinatesExtractor.apply(planet);
      assert planetCoords.getKind() == CoordinatesKindDto.PLANET;
      CoordinatesDto moonCoords = new CoordinatesDto(planetCoords.getGalaxy(), planetCoords.getSystem(),
          planetCoords.getPosition(), CoordinatesKindDto.MOON);
      moons.stream()
          .filter(m -> coordinatesExtractor.apply(m).equals(moonCoords))
          .findFirst()
          .ifPresent(bodies::add);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Body getUpdated(long bodyId) {
    Body body = bodyRepository.getOne(bodyId);
    updateResources(body, null);
    return body;
  }

  @Override
  @Transactional(readOnly = true)
  public ResourcesDto getResources(long bodyId) {
    Body body = getUpdated(bodyId);
    return Converter.convert(body.getResources());
  }

  @Override
  public void updateResources(Body body, Date at) {
    if (at == null) {
      at = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    }

    Date updatedAt = body.getUpdatedAt();

    if (!at.after(updatedAt)) {
      return;
    }

    if (body.getCoordinates().getKind() == CoordinatesKind.PLANET &&
        !userServiceInternal.isOnVacation(body.getUser())) {
      Resources resources = body.getResources();
      ProductionDto production = getProduction(body);
      ResourcesDto capacity = getCapacity(body);
      double seconds = at.toInstant().getEpochSecond() - updatedAt.toInstant().getEpochSecond();

      // Metal.
      double metalProduced = 0.0;
      if (production.getMetalProduction() != 0) {
        double metalCapacity = capacity.getMetal() - resources.getMetal();
        double fullProductionFor = Math.min(seconds, Math.max(0.0, metalCapacity *
            3600.0 / production.getMetalProduction()));
        metalProduced = (production.getMetalProduction() * fullProductionFor +
            production.getMetalBaseProduction() * (seconds - fullProductionFor)) / 3600.0;
      }

      // Crystal.
      double crystalProduced = 0.0;
      if (production.getCrystalProduction() != 0) {
        double crystalCapacity = capacity.getCrystal() - resources.getCrystal();
        double fullProductionFor = Math.min(seconds, Math.max(0.0, crystalCapacity *
            3600.0 / production.getCrystalProduction()));
        crystalProduced = (production.getCrystalProduction() * fullProductionFor +
            production.getCrystalBaseProduction() * (seconds - fullProductionFor)) / 3600.0;
      }

      // Deuterium.
      double deuteriumProduced = 0.0;
      if (production.getDeuteriumProduction() > 0) {
        double deuteriumCapacity = capacity.getDeuterium() - resources.getDeuterium();
        double fullProductionFor = Math.min(seconds, Math.max(0.0, deuteriumCapacity *
            3600.0 / production.getDeuteriumProduction()));
        deuteriumProduced = (production.getDeuteriumProduction() * fullProductionFor +
            production.getDeuteriumBaseProduction() * (seconds - fullProductionFor)) / 3600.0;
      } else if (production.getDeuteriumProduction() < 0) {
        deuteriumProduced = production.getDeuteriumProduction() * seconds / 3600.0;
      }

      // Update.
      Resources bodyResources = body.getResources();
      bodyResources.setMetal(bodyResources.getMetal() + metalProduced);
      bodyResources.setCrystal(bodyResources.getCrystal() + crystalProduced);
      bodyResources.setDeuterium(Math.max(0.0, bodyResources.getDeuterium() + deuteriumProduced));
    }

    body.setUpdatedAt(at);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductionDto getProduction(long bodyId) {
    Body body = bodyRepository.getOne(bodyId);
    return getProduction(body);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductionDto getProduction(Body body) {
    if (body.getCoordinates().getKind() != CoordinatesKind.PLANET) {
      return new ProductionDto(1.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    // Base production.
    int metalBaseProduction = this.metalBaseProduction * productionSpeed;
    int crystalBaseProduction = this.crystalBaseProduction * productionSpeed;
    int deuteriumBaseProduction = this.deuteriumBaseProduction * productionSpeed;

    ProductionItemsDto items = getProductionItems(body);
    ProductionFactors factors = body.getProductionFactors();

    // Metal mine.
    int metalMineLevel = items.getMetalMineLevel();
    double metalMineFactor = 0.1 * factors.getMetalMineFactor();
    int metalMineProduction = (int) (metalMineBaseProduction * metalMineLevel * Math.pow(1.1, metalMineLevel) *
        metalMineFactor) * productionSpeed;
    int metalMineMaxEnergyUsage = (int) Math.ceil(metalMineBaseEnergyUsage * metalMineLevel *
        Math.pow(1.1, metalMineLevel) * metalMineFactor);

    // Crystal mine.
    int crystalMineLevel = items.getCrystalMineLevel();
    double crystalMineFactor = 0.1 * factors.getCrystalMineFactor();
    int crystalMineProduction = (int) (crystalMineBaseProduction * crystalMineLevel *
        Math.pow(1.1, crystalMineLevel) * crystalMineFactor) * productionSpeed;
    int crystalMineMaxEnergyUsage = (int) Math.ceil(crystalMineBaseEnergyUsage * crystalMineLevel *
        Math.pow(1.1, crystalMineLevel) * crystalMineFactor);

    // Deuterium synthesizer.
    int deuteriumSynthesizerLevel = items.getDeuteriumSynthesizerLevel();
    double deuteriumSynthesizerFactor = 0.1 * factors.getDeuteriumSynthesizerFactor();
    int deuteriumSynthesizerProduction = (int) (deuteriumSynthesizerBaseProduction * deuteriumSynthesizerLevel *
        Math.pow(1.1, deuteriumSynthesizerLevel) * (1.28 - 0.002 * body.getTemperature()) *
        deuteriumSynthesizerFactor) * productionSpeed;
    int deuteriumSynthesizerMaxEnergyUsage = (int) Math.ceil(deuteriumSynthesizerBaseEnergyUsage *
        deuteriumSynthesizerLevel * Math.pow(1.1, deuteriumSynthesizerLevel) * deuteriumSynthesizerFactor);

    // Solar plant.
    int solarPlantLevel = items.getSolarPlantLevel();
    double solarPlantFactor = 0.1 * factors.getSolarPlantFactor();
    int solarPlantEnergyProduction = (int) (solarPlantBaseEnergyProduction * solarPlantLevel *
        Math.pow(1.1, solarPlantLevel) * solarPlantFactor);

    // Fusion reactor.
    int fusionReactorDeuteriumUsage = 0;
    int fusionReactorEnergyProduction = 0;
    int fusionReactorLevel = items.getFusionReactorLevel();
    if (fusionReactorLevel != 0) {
      // Query for energy technology only when necessary.
      Technology energyTechnology = body.getUser().getTechnologies().get(TechnologyKind.ENERGY_TECHNOLOGY);
      int energyTechnologyLevel = energyTechnology != null ? energyTechnology.getLevel() : 0;
      double fusionReactorFactor = 0.1 * factors.getFusionReactorFactor();
      fusionReactorDeuteriumUsage = (int) Math.ceil(fusionReactorBaseDeuteriumUsage * fusionReactorLevel *
          Math.pow(1.1, fusionReactorLevel) * fusionReactorFactor) * productionSpeed;
      fusionReactorEnergyProduction = (int) Math.round(Math.floor(fusionReactorBaseEnergyProduction *
          fusionReactorLevel * Math.pow(1.05 + 0.01 * energyTechnologyLevel, fusionReactorLevel)) *
          fusionReactorFactor);
    }

    // Solar satellites.
    int numSolarSatellites = items.getNumSolarSatellites();
    double solarSatellitesFactor = 0.1 * factors.getSolarSatellitesFactor();
    int singleSatelliteEnergy = Math.max(5, Math.min(50, (int) Math.floor(body.getTemperature() / 4.0 + 20.0)));
    int solarSatellitesEnergyProduction = (int) Math.round(singleSatelliteEnergy * numSolarSatellites *
        solarSatellitesFactor);

    // Energy balance.
    int totalEnergy = solarPlantEnergyProduction + fusionReactorEnergyProduction + solarSatellitesEnergyProduction;
    int usedEnergy = metalMineMaxEnergyUsage + crystalMineMaxEnergyUsage + deuteriumSynthesizerMaxEnergyUsage;
    int availableEnergy = totalEnergy - usedEnergy;
    double efficiency = usedEnergy == 0 ? 1.0 : Math.min(1.0, (double) totalEnergy / usedEnergy);

    // Current energy usage.
    int metalMineCurrentEnergyUsage = (int) (metalMineMaxEnergyUsage * efficiency);
    int crystalMineCurrentEnergyUsage = (int) (crystalMineMaxEnergyUsage * efficiency);
    int deuteriumSynthesizerCurrentEnergyUsage = (int) (deuteriumSynthesizerMaxEnergyUsage * efficiency);

    // Mines production with efficiency.
    metalMineProduction = (int) (metalMineProduction * efficiency);
    crystalMineProduction = (int) (crystalMineProduction * efficiency);
    deuteriumSynthesizerProduction = (int) (deuteriumSynthesizerProduction * efficiency);

    // Final production.
    int metalProduction = metalBaseProduction + metalMineProduction;
    int crystalProduction = crystalBaseProduction + crystalMineProduction;
    int deuteriumProduction = deuteriumBaseProduction + deuteriumSynthesizerProduction - fusionReactorDeuteriumUsage;

    return new ProductionDto(efficiency, metalBaseProduction, crystalBaseProduction, deuteriumBaseProduction,
        metalMineProduction, metalMineCurrentEnergyUsage, metalMineMaxEnergyUsage, crystalMineProduction,
        crystalMineCurrentEnergyUsage, crystalMineMaxEnergyUsage, deuteriumSynthesizerProduction,
        deuteriumSynthesizerCurrentEnergyUsage, deuteriumSynthesizerMaxEnergyUsage, solarPlantEnergyProduction,
        fusionReactorDeuteriumUsage, fusionReactorEnergyProduction, solarSatellitesEnergyProduction, metalProduction,
        crystalProduction, deuteriumProduction, totalEnergy, usedEnergy, availableEnergy);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductionItemsDto getProductionItems(long bodyId) {
    Body body = bodyRepository.getOne(bodyId);
    return getProductionItems(body);
  }

  private ProductionItemsDto getProductionItems(Body body) {
    int metalMineLevel = body.getBuildingLevel(BuildingKind.METAL_MINE);
    int crystalMineLevel = body.getBuildingLevel(BuildingKind.CRYSTAL_MINE);
    int deuteriumSynthesizerLevel = body.getBuildingLevel(BuildingKind.DEUTERIUM_SYNTHESIZER);
    int solarPlantLevel = body.getBuildingLevel(BuildingKind.SOLAR_PLANT);
    int fusionReactorLevel = body.getBuildingLevel(BuildingKind.FUSION_REACTOR);
    int numSolarSatellites = body.getNumUnits(UnitKind.SOLAR_SATELLITE);
    return new ProductionItemsDto(metalMineLevel, crystalMineLevel, deuteriumSynthesizerLevel, solarPlantLevel,
        fusionReactorLevel, numSolarSatellites);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductionFactorsDto getProductionFactors(long bodyId) {
    Body body = bodyRepository.getOne(bodyId);
    return Converter.convert(body.getProductionFactors());
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void setProductionFactors(long bodyId, ProductionFactorsDto factors) {
    Body body = getUpdated(bodyId);
    body.setProductionFactors(Converter.convert(factors));
  }

  @Override
  @Transactional(readOnly = true)
  public ResourcesDto getCapacity(long bodyId) {
    Body body = bodyRepository.getOne(bodyId);
    return getCapacity(body);
  }

  private ResourcesDto getCapacity(Body body) {
    double metal = getCapacity(body.getBuildingLevel(BuildingKind.METAL_STORAGE));
    double crystal = getCapacity(body.getBuildingLevel(BuildingKind.CRYSTAL_STORAGE));
    double deuterium = getCapacity(body.getBuildingLevel(BuildingKind.DEUTERIUM_TANK));
    return new ResourcesDto(metal, crystal, deuterium);
  }

  private double getCapacity(int level) {
    return Math.ceil(1 + Math.pow(1.6, level)) * 50000;
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "bodiesBasicInfo", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public void rename(long bodyId, String name) {
    Body body = bodyRepository.getOne(bodyId);
    body.setName(name);
  }

  @Override
  @Transactional
  public void setImage(long bodyId, int image) {
    Body body = bodyRepository.getOne(bodyId);
    if (body.getCoordinates().getKind() == CoordinatesKind.PLANET) {
      body.setImage(image);
    }
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  @CacheEvict(cacheNames = "bodiesBasicInfo", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public long abandonPlanet(long bodyId, String password) {
    if (!userServiceInternal.checkCurrentUserPassword(password)) {
      throw new WrongPasswordException();
    }

    Body body = bodyRepository.getOne(bodyId);
    if (body.getCoordinates().getKind() != CoordinatesKind.PLANET) {
      throw new WrongBodyKindException();
    }

    User user = body.getUser();

    long count = bodyRepository.countByUserAndCoordinatesKind(body.getUser(), CoordinatesKind.PLANET);
    if (count == 1) {
      throw new CannotDeleteLastPlanetException();
    }

    boolean techQueueEntryExists = user.getTechnologyQueue().values().stream()
        .anyMatch(e -> e.getBody().getId() == bodyId);
    if (techQueueEntryExists) {
      throw new TechnologyQueueEntryExistsException();
    }

    Coordinates moonCoords = new Coordinates(body.getCoordinates().getGalaxy(), body.getCoordinates().getSystem(),
        body.getCoordinates().getPosition(), CoordinatesKind.MOON);
    Optional<Body> moon = bodyRepository.findByCoordinates(moonCoords);

    List<Body> bodies = new ArrayList<>(Collections.singletonList(body));
    moon.ifPresent(bodies::add);

    if (flightServiceInternal.existsByStartOrTargetIn(bodies)) {
      throw new FlightsExistException();
    }

    bodies.forEach(this::delete);

    Optional<Long> first = user.getBodies().keySet().stream()
        .filter(id -> bodies.stream().noneMatch(b -> b.getId() == id))
        .findFirst();
    assert first.isPresent();
    return first.get();
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "bodiesBasicInfo", key = "#moon.user.id")
  public void destroyMoon(Body moon) {
    assert moon.getCoordinates().getKind() == CoordinatesKind.MOON;
    delete(moon);
  }

  private void delete(Body body) {
    buildingsServiceInternal.deleteBuildingsAndQueue(body);
    shipyardServiceInternal.deleteUnitsAndQueue(body);
    bodyRepository.delete(body);
  }
}
