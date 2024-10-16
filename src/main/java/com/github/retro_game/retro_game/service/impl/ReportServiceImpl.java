package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.repository.*;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.ActivityService;
import com.github.retro_game.retro_game.service.exception.ReportDoesNotExistException;
import com.github.retro_game.retro_game.service.exception.UnauthorizedReportAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("reportService")
class ReportServiceImpl implements ReportServiceInternal {
  private static final int TOKEN_BITS = 128;
  private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
  private final EspionageReportRepository espionageReportRepository;
  private final HarvestReportRepository harvestReportRepository;
  private final OtherReportRepository otherReportRepository;
  private final SimplifiedCombatReportRepository simplifiedCombatReportRepository;
  private final TransportReportRepository transportReportRepository;
  private final UserRepository userRepository;
  private BodyServiceInternal bodyServiceInternal;
  private ActivityService activityService;

  public ReportServiceImpl(EspionageReportRepository espionageReportRepository,
                           HarvestReportRepository harvestReportRepository, OtherReportRepository otherReportRepository,
                           SimplifiedCombatReportRepository simplifiedCombatReportRepository,
                           TransportReportRepository transportReportRepository, UserRepository userRepository) {
    this.espionageReportRepository = espionageReportRepository;
    this.harvestReportRepository = harvestReportRepository;
    this.otherReportRepository = otherReportRepository;
    this.simplifiedCombatReportRepository = simplifiedCombatReportRepository;
    this.transportReportRepository = transportReportRepository;
    this.userRepository = userRepository;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Autowired
  public void setActivityService(ActivityService activityService) {
    this.activityService = activityService;
  }

  @PostConstruct
  private void check() {
    Assert.isTrue(CoordinatesKind.values().length <= 256,
        "Too many values in CoordinatesKind, combat report serialization won't work");
    Assert.isTrue(UnitKind.values().length <= 256,
        "Too many values in UnitKind, combat report serialization won't work");
  }

  @Override
  @Cacheable(cacheNames = "reportsSummaries",
      key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public ReportsSummaryDto getSummary(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    int numCombatReports = simplifiedCombatReportRepository.countByUserAndDeletedIsFalseAndAtAfter(user,
        user.getCombatReportsSeenAt());
    int numEspionageReports = espionageReportRepository.countByUserAndDeletedIsFalseAndAtAfter(user,
        user.getEspionageReportsSeenAt());
    int numHarvestReports = harvestReportRepository.countByUserAndDeletedIsFalseAndAtAfter(user,
        user.getHarvestReportsSeenAt());
    int numTransportReports = transportReportRepository.countByUserAndDeletedIsFalseAndAtAfter(user,
        user.getTransportReportsSeenAt());
    int numOtherReports = otherReportRepository.countByUserAndDeletedIsFalseAndAtAfter(user,
        user.getOtherReportsSeenAt());
    return new ReportsSummaryDto(numCombatReports, numEspionageReports, numHarvestReports, numTransportReports,
        numOtherReports);
  }

  @Override
  @CacheEvict(cacheNames = "reportsSummaries", key = "#user.id")
  public void createSimplifiedCombatReport(User user, boolean isAttacker, Date at, User enemy, Coordinates coordinates,
                                           BattleResult result, int numRounds, Resources attackersLoss,
                                           Resources defendersLoss, Resources plunder, Resources debris,
                                           MoonCreationResultDto moonCreationResult, CombatReport combatReport) {
    // 0 rounds (no battle at all) implies attackers win.
    assert numRounds != 0 || result == BattleResult.ATTACKERS_WIN;
    // Draw implies 6 rounds.
    assert result != BattleResult.DRAW || numRounds == 6;

    boolean lostContact = isAttacker && result == BattleResult.DEFENDERS_WIN && numRounds == 1;

    CombatResult res;
    if (result == BattleResult.DRAW) {
      res = CombatResult.DRAW;
    } else if ((isAttacker && result == BattleResult.ATTACKERS_WIN) ||
        (!isAttacker && result == BattleResult.DEFENDERS_WIN)) {
      res = CombatResult.WIN;
    } else {
      res = CombatResult.LOSS;
    }

    long aLoss = (long) (attackersLoss.getMetal() + attackersLoss.getCrystal() + attackersLoss.getDeuterium());
    long dLoss = (long) (defendersLoss.getMetal() + defendersLoss.getCrystal() + defendersLoss.getDeuterium());

    var combatReportId = !lostContact && combatReport != null ? combatReport.getId() : null;
    var report =
        new SimplifiedCombatReport(0, user, false, at, enemy.getId(), enemy.getName(), coordinates, res, aLoss, dLoss,
            plunder, (long) debris.getMetal(), (long) debris.getCrystal(), moonCreationResult.chance(),
            moonCreationResult.created(), combatReportId);
    simplifiedCombatReportRepository.save(report);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "reportsSummaries",
      key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public List<SimplifiedCombatReportDto> getSimplifiedCombatReports(long bodyId,
                                                                    SimplifiedCombatReportSortOrderDto sortOrder,
                                                                    Sort.Direction direction, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    user.setCombatReportsSeenAt(Date.from(Instant.now()));

    var reports = simplifiedCombatReportRepository.findReports(user, Converter.convert(sortOrder), direction, pageable);
    var ret = new ArrayList<SimplifiedCombatReportDto>(reports.size());
    for (var report : reports) {
      ret.add(new SimplifiedCombatReportDto(report.getId(), report.getAt(), report.getEnemyId(), report.getEnemyName(),
          Converter.convert(report.getCoordinates()), Converter.convert(report.getResult()), report.getAttackersLoss(),
          report.getDefendersLoss(), Converter.convert(report.getPlunder()), report.getDebrisMetal(),
          report.getDebrisCrystal(), report.getMoonChance(), report.isMoonGiven(), report.getCombatReportId()));
    }
    return ret;
  }

  @Override
  @Transactional
  public void deleteSimplifiedCombatReport(long bodyId, long reportId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<SimplifiedCombatReport> reportOptional = simplifiedCombatReportRepository.findById(reportId);
    if (!reportOptional.isPresent()) {
      logger.warn("Deleting simplified combat report failed, report doesn't exist: userId={} reportId={}",
          userId, reportId);
      throw new ReportDoesNotExistException();
    }
    SimplifiedCombatReport report = reportOptional.get();

    if (report.getUser().getId() != userId) {
      logger.warn("Deleting simplified combat report failed, unauthorized access: userId={} reportId={}",
          userId, reportId);
      throw new UnauthorizedReportAccessException();
    }

    logger.info("Deleting simplified combat report: userId={} reportId={}", userId, reportId);
    report.setDeleted(true);
  }

  @Override
  @Transactional
  public void deleteAllSimplifiedCombatReports(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    simplifiedCombatReportRepository.markAllAsDeletedByUserId(userId);
    logger.info("Deleting all simplified combat reports: userId={}", userId);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  @CacheEvict(cacheNames = "reportsSummaries", key = "#flight.startUser.id")
  public void createEspionageReport(Flight flight, List<Flight> holdingFlights, double counterChance) {
    try {
      Body body = flight.getTargetBody();
      bodyServiceInternal.updateResourcesAndShipyard(body, flight.getArrivalAt());

      long now = flight.getArrivalAt().toInstant().getEpochSecond();
      Long activityAt = activityService.getBodyActivity(body.getId());
      if (activityAt == null) {
        activityAt = 0L;
      }
      int activity = (int) ((now - activityAt) / 60L);
      if (activity < 15) {
        activity = 0;
      } else if (activity >= 60) {
        activity = 60;
      }

      var numProbes = flight.getUnitsCount(UnitKind.ESPIONAGE_PROBE);

      var targetLevel = body.getUser().getTechnologyLevel(TechnologyKind.ESPIONAGE_TECHNOLOGY);
      var ownLevel = flight.getStartUser().getTechnologyLevel(TechnologyKind.ESPIONAGE_TECHNOLOGY);
      int diff = targetLevel - ownLevel;
      int n = diff * Math.abs(diff);

      boolean fleetVisible = numProbes >= Math.max(n + 2, 1);
      boolean defenseVisible = numProbes >= Math.max(n + 3, 1);
      boolean buildingsVisible = numProbes >= Math.max(n + 5, 1);
      boolean technologiesVisible = numProbes >= Math.max(n + 7, 1);

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      DataOutputStream stream = new DataOutputStream(byteArrayOutputStream);

      var units = body.getUnits();

      stream.writeBoolean(fleetVisible);
      Long fleet = null;
      if (fleetVisible) {
        var bodyUnitsStream = units.entrySet().stream()
            .filter(e -> UnitItem.getFleet().containsKey(e.getKey()));
        var holdingUnitsStream = holdingFlights.stream().flatMap(f -> f.getUnits().entrySet().stream());
        var fleetUnits = Stream.concat(bodyUnitsStream, holdingUnitsStream)
            .filter(e -> e.getValue() > 0)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Integer::sum,
                () -> new EnumMap<>(UnitKind.class)
            ));
        fleet = calculateUnitsCost(fleetUnits);
        serializeEnumMap(stream, fleetUnits);
      }

      stream.writeBoolean(defenseVisible);
      Long defense = null;
      if (defenseVisible) {
        var defenseUnits = units.entrySet().stream()
            .filter(e -> e.getValue() > 0 && UnitItem.getDefense().containsKey(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
              throw new IllegalStateException();
            }, () -> new EnumMap<>(UnitKind.class)));
        defense = calculateUnitsCost(defenseUnits);
        serializeEnumMap(stream, defenseUnits);
      }

      stream.writeBoolean(buildingsVisible);
      if (buildingsVisible) {
        var buildings = body.getBuildings().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
              throw new IllegalStateException();
            }, () -> new EnumMap<>(BuildingKind.class)));
        serializeEnumMap(stream, buildings);
      }

      stream.writeBoolean(technologiesVisible);
      if (technologiesVisible) {
        var technologies = body.getUser().getTechnologies().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
              throw new IllegalStateException();
            }, () -> new EnumMap<>(TechnologyKind.class)));
        serializeEnumMap(stream, technologies);
      }

      EspionageReport report = new EspionageReport();
      report.setUser(flight.getStartUser());
      report.setDeleted(false);
      report.setAt(flight.getArrivalAt());
      report.setEnemyId(body.getUser().getId());
      report.setEnemyName(body.getUser().getName());
      report.setCoordinates(body.getCoordinates());
      report.setActivity(activity);
      report.setResources(body.getResources());
      report.setFleet(fleet);
      report.setDefense(defense);
      report.setDiameter(body.getDiameter());
      report.setCounterChance(counterChance);
      report.setToken(generateRandomToken());
      report.setData(byteArrayOutputStream.toByteArray());
      espionageReportRepository.save(report);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public EspionageReportDto getEspionageReport(long id, String token) {
    long userId = 0;
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof CustomUser) {
      userId = ((CustomUser) auth.getPrincipal()).getUserId();
    }

    Optional<EspionageReport> reportOptional = espionageReportRepository.findById(id);
    if (!reportOptional.isPresent()) {
      logger.warn("Getting espionage report failed, report doesn't exist: userId={} reportId={}", userId, id);
      throw new ReportDoesNotExistException();
    }
    EspionageReport report = reportOptional.get();

    byte[] t = Base64.getUrlDecoder().decode(token);
    if (!MessageDigest.isEqual(t, report.getToken())) {
      logger.warn("Getting espionage report failed, wrong token: userId={} reportId={}", userId, id);
      throw new ReportDoesNotExistException();
    }

    logger.info("Getting espionage report: userId={} reportId={}", userId, id);
    try {
      DataInputStream stream = new DataInputStream(new ByteArrayInputStream(report.getData()));

      Map<UnitKindDto, Integer> fleet = null;
      boolean fleetVisible = stream.readBoolean();
      if (fleetVisible) {
        fleet = Converter.convertToEnumMap(deserializeEnumMap(stream, UnitKind.class), UnitKindDto.class,
            Converter::convert, Function.identity());
      }

      Map<UnitKindDto, Integer> defense = null;
      boolean defenseVisible = stream.readBoolean();
      if (defenseVisible) {
        defense = Converter.convertToEnumMap(deserializeEnumMap(stream, UnitKind.class), UnitKindDto.class,
            Converter::convert, Function.identity());
      }

      Map<BuildingKindDto, Integer> buildings = null;
      boolean buildingsVisible = stream.readBoolean();
      if (buildingsVisible) {
        buildings = Converter.convertToEnumMap(deserializeEnumMap(stream, BuildingKind.class), BuildingKindDto.class,
            Converter::convert, Function.identity());
      }

      Map<TechnologyKindDto, Integer> technologies = null;
      boolean technologiesVisible = stream.readBoolean();
      if (technologiesVisible) {
        technologies = Converter.convertToEnumMap(deserializeEnumMap(stream, TechnologyKind.class),
            TechnologyKindDto.class, Converter::convert, Function.identity());
      }

      return new EspionageReportDto(report.getAt(), report.getEnemyId(), report.getEnemyName(),
          Converter.convert(report.getCoordinates()), report.getDiameter(), report.getActivity(),
          report.getCounterChance(), Converter.convert(report.getResources()), fleet, defense, buildings, technologies);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "reportsSummaries",
      key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public List<SimplifiedEspionageReportDto> getSimplifiedEspionageReports(long bodyId,
                                                                          EspionageReportSortOrderDto sortOrder,
                                                                          Sort.Direction direction, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    user.setEspionageReportsSeenAt(Date.from(Instant.now()));

    List<EspionageReport> reports = espionageReportRepository.findReports(user, Converter.convert(sortOrder), direction,
        pageable);
    List<SimplifiedEspionageReportDto> simplifiedReports = new ArrayList<>(reports.size());
    for (EspionageReport report : reports) {

      String token = Base64.getUrlEncoder().withoutPadding().encodeToString(report.getToken());

      Resources resources = report.getResources();
      double neededCapacity = 0.5 * Math.max(
          resources.getMetal() + resources.getCrystal() + resources.getDeuterium(),
          Math.min(
              0.75 * (2.0 * resources.getMetal() + resources.getCrystal() + resources.getDeuterium()),
              2.0 * resources.getMetal() + resources.getDeuterium()));
      int neededSmallCargoes = (int) Math.ceil(
          neededCapacity / UnitItem.getFleet().get(UnitKind.SMALL_CARGO).getCapacity());
      int neededLargeCargoes = (int) Math.ceil(
          neededCapacity / UnitItem.getFleet().get(UnitKind.LARGE_CARGO).getCapacity());
      int neededEspionageProbes = (int) Math.ceil(
          neededCapacity / UnitItem.getFleet().get(UnitKind.ESPIONAGE_PROBE).getCapacity());

      simplifiedReports.add(new SimplifiedEspionageReportDto(report.getId(), report.getAt(), report.getEnemyId(),
          report.getEnemyName(), Converter.convert(report.getCoordinates()), report.getActivity(),
          Converter.convert(resources), report.getFleet(), report.getDefense(), token, neededSmallCargoes,
          neededLargeCargoes, neededEspionageProbes));
    }
    return simplifiedReports;
  }

  @Override
  @Transactional
  public void deleteEspionageReport(long bodyId, long reportId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<EspionageReport> reportOptional = espionageReportRepository.findById(reportId);
    if (!reportOptional.isPresent()) {
      logger.warn("Deleting espionage report failed, report doesn't exist: userId={} reportId={}", userId, reportId);
      throw new ReportDoesNotExistException();
    }
    EspionageReport report = reportOptional.get();

    if (report.getUser().getId() != userId) {
      logger.warn("Deleting espionage report failed, unauthorized access: userId={} reportId={}", userId, reportId);
      throw new UnauthorizedReportAccessException();
    }

    logger.info("Deleting espionage report: userId={} reportId={}", userId, reportId);
    report.setDeleted(true);
  }

  @Override
  @Transactional
  public void deleteAllEspionageReports(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    espionageReportRepository.markAllAsDeletedByUserId(userId);
    logger.info("Deleting all espionage reports: userId={}", userId);
  }

  private long calculateUnitsCost(Map<UnitKind, Integer> units) {
    long sum = 0;
    for (Map.Entry<UnitKind, Integer> entry : units.entrySet()) {
      UnitItem item = UnitItem.getAll().get(entry.getKey());
      Resources cost = item.getCost();
      sum += entry.getValue() * (long) (cost.getMetal() + cost.getCrystal() + cost.getDeuterium());
    }
    return sum;
  }

  private static <K extends Enum<K>> void serializeEnumMap(DataOutputStream stream, EnumMap<K, Integer> map)
      throws IOException {
    stream.writeByte(map.size());
    for (Map.Entry<K, Integer> entry : map.entrySet()) {
      stream.writeByte(entry.getKey().ordinal());
      stream.writeInt(entry.getValue());
    }
  }

  private static <K extends Enum<K>> EnumMap<K, Integer> deserializeEnumMap(DataInputStream stream, Class<K> clazz)
      throws IOException {
    EnumMap<K, Integer> map = new EnumMap<>(clazz);
    int size = stream.readByte();
    for (int i = 0; i < size; i++) {
      K kind = clazz.getEnumConstants()[stream.readByte()];
      int count = stream.readInt();
      map.put(kind, count);
    }
    return map;
  }

  @Override
  @CacheEvict(cacheNames = "reportsSummaries", key = "#flight.startUser.id")
  public void createHarvestReport(Flight flight, int numRecyclers, long capacity, long harvestedMetal,
                                  long harvestedCrystal, long remainingMetal, long remainingCrystal) {
    HarvestReport report = new HarvestReport();
    report.setUser(flight.getStartUser());
    report.setDeleted(false);
    report.setAt(flight.getArrivalAt());
    report.setCoordinates(flight.getTargetCoordinates());
    report.setNumRecyclers(numRecyclers);
    report.setCapacity(capacity);
    report.setHarvestedMetal(harvestedMetal);
    report.setHarvestedCrystal(harvestedCrystal);
    report.setRemainingMetal(remainingMetal);
    report.setRemainingCrystal(remainingCrystal);
    harvestReportRepository.save(report);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "reportsSummaries",
      key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public List<HarvestReportDto> getHarvestReports(long bodyId, HarvestReportSortOrderDto sortOrder,
                                                  Sort.Direction direction, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    user.setHarvestReportsSeenAt(Date.from(Instant.now()));
    return harvestReportRepository.findReports(user, Converter.convert(sortOrder), direction, pageable).stream()
        .map(Converter::convert)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteHarvestReport(long bodyId, long reportId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<HarvestReport> reportOptional = harvestReportRepository.findById(reportId);
    if (!reportOptional.isPresent()) {
      logger.warn("Deleting harvest report failed, report doesn't exist: userId={} reportId={}", userId, reportId);
      throw new ReportDoesNotExistException();
    }
    HarvestReport report = reportOptional.get();

    if (report.getUser().getId() != userId) {
      logger.warn("Deleting harvest report failed, unauthorized access: userId={} reportId={}", userId, reportId);
      throw new UnauthorizedReportAccessException();
    }

    logger.info("Deleting harvest report: userId={} reportId={}", userId, reportId);
    report.setDeleted(true);
  }

  @Override
  @Transactional
  public void deleteAllHarvestReports(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    harvestReportRepository.markAllAsDeletedByUserId(userId);
    logger.info("Deleting all harvest reports: userId={}", userId);
  }

  @Override
  @CacheEvict(cacheNames = "reportsSummaries", key = "#user.id")
  public void createTransportReport(Flight flight, User user, User partner, Resources resources) {
    TransportKind kind;
    if (user.getId() == partner.getId()) {
      kind = TransportKind.OWN;
    } else if (flight.getStartUser().getId() == user.getId()) {
      kind = TransportKind.OUTGOING;
    } else {
      kind = TransportKind.INCOMING;
    }

    TransportReport report = new TransportReport();
    report.setUser(user);
    report.setAt(flight.getArrivalAt());
    report.setDeleted(false);
    report.setKind(kind);
    report.setPartnerId(partner.getId());
    report.setPartnerName(partner.getName());
    report.setStartCoordinates(flight.getStartBody().getCoordinates());
    report.setTargetCoordinates(flight.getTargetCoordinates());
    report.setResources(resources);
    transportReportRepository.save(report);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "reportsSummaries",
      key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public List<TransportReportDto> getTransportReports(long bodyId, TransportReportSortOrderDto sortOrder,
                                                      Sort.Direction direction, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    user.setTransportReportsSeenAt(Date.from(Instant.now()));
    return transportReportRepository.findReports(user, Converter.convert(sortOrder), direction, pageable).stream()
        .map(Converter::convert)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteTransportReport(long bodyId, long reportId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<TransportReport> reportOptional = transportReportRepository.findById(reportId);
    if (!reportOptional.isPresent()) {
      logger.warn("Deleting transport report failed, report doesn't exist: userId={} reportId={}", userId, reportId);
      throw new ReportDoesNotExistException();
    }
    TransportReport report = reportOptional.get();

    if (report.getUser().getId() != userId) {
      logger.warn("Deleting transport report failed, unauthorized access: userId={} reportId={}", userId, reportId);
      throw new UnauthorizedReportAccessException();
    }

    logger.info("Deleting transport report: userId={} reportId={}", userId, reportId);
    report.setDeleted(true);
  }

  @Override
  @Transactional
  public void deleteAllTransportReports(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    transportReportRepository.markAllAsDeletedByUserId(userId);
    logger.info("Deleting all transport reports: userId={}", userId);
  }

  @Override
  @CacheEvict(cacheNames = "reportsSummaries", key = "#flight.startUser.id")
  public void createColonizationReport(Flight flight, @Nullable Resources resources, @Nullable Double diameter) {
    OtherReport report = new OtherReport();
    report.setUser(flight.getStartUser());
    report.setDeleted(false);
    report.setAt(flight.getArrivalAt());
    report.setKind(OtherReportKind.COLONIZATION);
    report.setStartCoordinates(flight.getStartBody().getCoordinates());
    report.setTargetCoordinates(flight.getTargetCoordinates());
    report.setResources(resources);
    report.setParam(diameter);
    otherReportRepository.save(report);
  }

  @Override
  @CacheEvict(cacheNames = "reportsSummaries", key = "#flight.startUser.id")
  public void createDeploymentReport(Flight flight) {
    OtherReport report = new OtherReport();
    report.setUser(flight.getStartUser());
    report.setDeleted(false);
    report.setAt(flight.getArrivalAt());
    report.setKind(OtherReportKind.DEPLOYMENT);
    report.setStartCoordinates(flight.getStartBody().getCoordinates());
    report.setTargetCoordinates(flight.getTargetCoordinates());
    report.setResources(flight.getResources());
    otherReportRepository.save(report);
  }

  @Override
  @CacheEvict(cacheNames = "reportsSummaries", key = "#flight.targetUser.id")
  public void createHostileEspionageReport(Flight flight, double counterEspionageChance) {
    OtherReport report = new OtherReport();
    report.setUser(flight.getTargetUser());
    report.setDeleted(false);
    report.setAt(flight.getArrivalAt());
    report.setKind(OtherReportKind.HOSTILE_ESPIONAGE);
    report.setStartCoordinates(flight.getStartBody().getCoordinates());
    report.setTargetCoordinates(flight.getTargetCoordinates());
    report.setParam(counterEspionageChance);
    otherReportRepository.save(report);
  }

  @Override
  @CacheEvict(cacheNames = "reportsSummaries", key = "#flight.startUser.id")
  public void createReturnReport(Flight flight) {
    OtherReport report = new OtherReport();
    report.setUser(flight.getStartUser());
    report.setDeleted(false);
    report.setAt(flight.getReturnAt());
    report.setKind(OtherReportKind.RETURN);
    report.setStartCoordinates(flight.getStartBody().getCoordinates());
    report.setTargetCoordinates(flight.getTargetCoordinates());
    report.setResources(flight.getResources());
    otherReportRepository.save(report);
  }

  @Override
  @CacheEvict(cacheNames = "reportsSummaries", key = "#flight.targetUser.id")
  public void createMissileAttackReport(Flight flight, int totalDestroyed) {
    OtherReport report = new OtherReport();
    report.setUser(flight.getTargetUser());
    report.setDeleted(false);
    report.setAt(flight.getArrivalAt());
    report.setKind(OtherReportKind.MISSILE_ATTACK);
    report.setStartCoordinates(flight.getStartBody().getCoordinates());
    report.setTargetCoordinates(flight.getTargetCoordinates());
    report.setParam((double) totalDestroyed);
    otherReportRepository.save(report);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "reportsSummaries",
      key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public List<OtherReportDto> getOtherReports(long bodyId, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    user.setOtherReportsSeenAt(Date.from(Instant.now()));
    PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC,
        "at");
    return otherReportRepository.findAllByUserAndDeletedIsFalse(user, pageRequest).stream()
        .map(Converter::convert)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteOtherReport(long bodyId, long reportId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<OtherReport> reportOptional = otherReportRepository.findById(reportId);
    if (!reportOptional.isPresent()) {
      logger.warn("Deleting other report failed, report doesn't exist: userId={} reportId={}", userId, reportId);
      throw new ReportDoesNotExistException();
    }
    OtherReport report = reportOptional.get();

    if (report.getUser().getId() != userId) {
      logger.warn("Deleting other report failed, unauthorized access: userId={} reportId={}", userId, reportId);
      throw new UnauthorizedReportAccessException();
    }

    logger.info("Deleting other report: userId={} reportId={}", userId, reportId);
    report.setDeleted(true);
  }

  @Override
  @Transactional
  public void deleteAllOtherReports(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    otherReportRepository.markAllAsDeletedByUserId(userId);
    logger.info("Deleting all other reports: userId={}", userId);
  }

  private byte[] generateRandomToken() {
    byte[] token = new byte[TOKEN_BITS / 8];
    ThreadLocalRandom.current().nextBytes(token);
    return token;
  }
}
