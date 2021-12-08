package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.battleengine.BattleOutcome;
import com.github.retro_game.retro_game.battleengine.Combatant;
import com.github.retro_game.retro_game.battleengine.CombatantOutcome;
import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.repository.*;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.ActivityService;
import com.github.retro_game.retro_game.service.exception.ReportDoesntExistException;
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
  private final CombatReportRepository combatReportRepository;
  private final EspionageReportRepository espionageReportRepository;
  private final HarvestReportRepository harvestReportRepository;
  private final OtherReportRepository otherReportRepository;
  private final SimplifiedCombatReportRepository simplifiedCombatReportRepository;
  private final TransportReportRepository transportReportRepository;
  private final UserRepository userRepository;
  private BodyServiceInternal bodyServiceInternal;
  private ActivityService activityService;

  public ReportServiceImpl(CombatReportRepository combatReportRepository,
                           EspionageReportRepository espionageReportRepository,
                           HarvestReportRepository harvestReportRepository, OtherReportRepository otherReportRepository,
                           SimplifiedCombatReportRepository simplifiedCombatReportRepository,
                           TransportReportRepository transportReportRepository, UserRepository userRepository) {
    this.combatReportRepository = combatReportRepository;
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
  @Cacheable(cacheNames = "reportsSummaries", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
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
  @Transactional
  public CombatReport createCombatReport(Date at, List<Combatant> attackers, List<Combatant> defenders,
                                         BattleOutcome battleOutcome, BattleResult result, Resources attackersLoss,
                                         Resources defendersLoss, Resources plunder, long debrisMetal,
                                         long debrisCrystal, double moonChance, boolean moonGiven, int seed,
                                         long executionTime) {
    long aLoss = (long) (attackersLoss.getMetal() + attackersLoss.getCrystal() + attackersLoss.getDeuterium());
    long dLoss = (long) (defendersLoss.getMetal() + defendersLoss.getCrystal() + defendersLoss.getDeuterium());

    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      DataOutputStream stream = new DataOutputStream(byteArrayOutputStream);

      storeCombatants(stream, attackers);
      storeCombatants(stream, defenders);

      int numRounds = battleOutcome.getNumRounds();
      assert numRounds >= 1 && numRounds <= 256;
      stream.writeByte(numRounds - 1);

      var attackersOutcomes = battleOutcome.getAttackersOutcomes();
      var defendersOutcomes = battleOutcome.getDefendersOutcomes();
      for (int round = 0; round < numRounds; round++) {
        storeCombatantsOutcomes(stream, attackers, attackersOutcomes, round);
        storeCombatantsOutcomes(stream, defenders, defendersOutcomes, round);
      }

      CombatReport report = new CombatReport();
      report.setToken(generateRandomToken());
      report.setAt(at);
      report.setResult(result);
      report.setAttackersLoss(aLoss);
      report.setDefendersLoss(dLoss);
      report.setPlunder(plunder);
      report.setDebrisMetal(debrisMetal);
      report.setDebrisCrystal(debrisCrystal);
      report.setMoonChance(moonChance);
      report.setMoonGiven(moonGiven);
      report.setSeed(seed);
      report.setExecutionTime(executionTime);
      report.setData(byteArrayOutputStream.toByteArray());
      return combatReportRepository.save(report);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public CombatReportDto getCombatReport(long id, String token) {
    long userId = 0;
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof CustomUser) {
      userId = ((CustomUser) auth.getPrincipal()).getUserId();
    }

    Optional<CombatReport> reportOptional = combatReportRepository.findById(id);
    if (!reportOptional.isPresent()) {
      logger.warn("Getting combat report failed, report doesn't exist: userId={} reportId={}", userId, id);
      throw new ReportDoesntExistException();
    }
    CombatReport report = reportOptional.get();

    byte[] t = Base64.getUrlDecoder().decode(token);
    if (!MessageDigest.isEqual(t, report.getToken())) {
      logger.warn("Getting combat report failed, wrong token: userId={} reportId={}", userId, id);
      throw new ReportDoesntExistException();
    }

    logger.info("Getting combat report: userId={} reportId={}", userId, id);
    try {
      byte[] data = report.getData();

      DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
      Map<Long, User> users = findUsers(stream);

      stream = new DataInputStream(new ByteArrayInputStream(data));
      List<CombatReportCombatantDto> attackers = loadCombatants(stream, users);
      List<CombatReportCombatantDto> defenders = loadCombatants(stream, users);
      List<CombatReportRoundDto> rounds = loadRounds(stream, users);

      return new CombatReportDto(report.getAt(), attackers, defenders, rounds, Converter.convert(report.getResult()),
          report.getAttackersLoss(), report.getDefendersLoss(), Converter.convert(report.getPlunder()),
          report.getDebrisMetal(), report.getDebrisCrystal(), report.getMoonChance(), report.isMoonGiven(),
          report.getSeed(), report.getExecutionTime());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void storeCombatants(DataOutputStream stream, List<Combatant> combatants) throws IOException {
    int numCombatants = combatants.size();
    assert numCombatants >= 1 && numCombatants <= 256;
    stream.writeByte(numCombatants - 1);
    for (Combatant combatant : combatants) {
      storeCombatant(stream, combatant);
    }
  }

  private List<CombatReportCombatantDto> loadCombatants(DataInputStream stream, Map<Long, User> users)
      throws IOException {
    int numCombatants = stream.readUnsignedByte() + 1;
    List<CombatReportCombatantDto> combatants = new ArrayList<>(numCombatants);
    for (int i = 0; i < numCombatants; i++) {
      combatants.add(loadCombatant(stream, users));
    }
    return combatants;
  }

  private void storeCombatant(DataOutputStream stream, Combatant combatant) throws IOException {
    stream.writeLong(combatant.getUserId());

    Coordinates coords = combatant.getCoordinates();
    stream.writeInt(coords.getGalaxy());
    stream.writeInt(coords.getSystem());
    stream.writeInt(coords.getPosition());
    stream.writeByte(coords.getKind().ordinal());

    stream.writeByte(combatant.getWeaponsTechnology());
    stream.writeByte(combatant.getShieldingTechnology());
    stream.writeByte(combatant.getArmorTechnology());

    var unitGroups = combatant.getUnitGroups();
    assert UnitKind.values().length <= Byte.MAX_VALUE;
    stream.writeByte((int) unitGroups.values().stream().filter(i -> i != 0).count() - 1);
    for (var entry : unitGroups.entrySet()) {
      var kind = entry.getKey();
      var count = entry.getValue();
      if (count != 0) {
        assert count > 0;
        stream.writeByte(kind.ordinal());
        stream.writeLong(count);
      }
    }
  }

  private CombatReportCombatantDto loadCombatant(DataInputStream stream, Map<Long, User> users) throws IOException {
    long id = stream.readLong();
    assert id > 0;
    User user = users.get(id);
    String name = user != null ? user.getName() : "[deleted]";

    int g = stream.readInt();
    int s = stream.readInt();
    int p = stream.readInt();
    CoordinatesKind k = CoordinatesKind.values()[stream.readUnsignedByte()];
    CoordinatesDto coords = Converter.convert(new Coordinates(g, s, p, k));

    int weaponsTechnology = stream.readUnsignedByte();
    int shieldingTechnology = stream.readUnsignedByte();
    int armorTechnology = stream.readUnsignedByte();

    int numGroups = stream.readUnsignedByte() + 1;
    Map<UnitKindDto, CombatReportUnitGroupDto> unitGroups = new EnumMap<>(UnitKindDto.class);
    for (int i = 0; i < numGroups; i++) {
      UnitKind kind = UnitKind.values()[stream.readUnsignedByte()];
      long numUnits = stream.readLong();
      assert numUnits > 0;

      UnitItem item = UnitItem.getAll().get(kind);
      double weapons = (1.0 + 0.1 * weaponsTechnology) * item.getBaseWeapons();
      double shields = (1.0 + 0.1 * shieldingTechnology) * item.getBaseShield();
      double armor = (1.0 + 0.1 * armorTechnology) * item.getBaseArmor();

      unitGroups.put(Converter.convert(kind), new CombatReportUnitGroupDto(numUnits, weapons, shields, armor));
    }

    return new CombatReportCombatantDto(name, coords, weaponsTechnology, shieldingTechnology, armorTechnology,
        unitGroups);
  }

  private void storeCombatantsOutcomes(DataOutputStream stream, List<Combatant> combatants,
                                       List<CombatantOutcome> outcomes, int round) throws IOException {
    int[] numActiveGroups = outcomes.stream()
        .mapToInt(outcome -> (int) outcome.getNthRoundUnitGroupsStats(round).values().stream()
            .filter(s -> s.getTimesFired() > 0)
            .count())
        .toArray();

    int numActiveCombatants = (int) Arrays.stream(numActiveGroups)
        .filter(n -> n != 0)
        .count();
    assert numActiveCombatants >= 1 && numActiveCombatants <= 256;
    stream.writeByte(numActiveCombatants - 1);

    for (int i = 0; i < outcomes.size(); i++) {
      if (numActiveGroups[i] != 0) {
        var combatant = combatants.get(i);
        stream.writeLong(combatant.getUserId());

        assert numActiveGroups[i] <= 256;
        stream.writeByte(numActiveGroups[i] - 1);

        var outcome = outcomes.get(i);
        var unitGroupsStats = outcome.getNthRoundUnitGroupsStats(round);
        for (var entry : unitGroupsStats.entrySet()) {
          var kind = entry.getKey();
          var stats = entry.getValue();
          var active = stats.getTimesFired() > 0;
          if (active) {
            stream.writeByte(kind.ordinal());
            stream.writeLong(stats.getTimesFired());
            stream.writeLong(stats.getTimesWasShot());
            stream.writeLong((long) stats.getShieldDamageDealt());
            stream.writeLong((long) stats.getHullDamageDealt());
            stream.writeLong((long) stats.getShieldDamageTaken());
            stream.writeLong((long) stats.getHullDamageTaken());
            stream.writeLong(stats.getNumRemainingUnits());
          }
        }
      }
    }
  }

  private List<CombatReportRoundDto> loadRounds(DataInputStream stream, Map<Long, User> users) throws IOException {
    int numRounds = stream.readUnsignedByte() + 1;
    List<CombatReportRoundDto> rounds = new ArrayList<>(numRounds);
    for (int i = 0; i < numRounds; i++) {
      List<CombatReportRoundCombatantDto> attackers = loadRoundCombatants(stream, users);
      List<CombatReportRoundCombatantDto> defenders = loadRoundCombatants(stream, users);
      rounds.add(new CombatReportRoundDto(attackers, defenders));
    }
    return rounds;
  }

  private List<CombatReportRoundCombatantDto> loadRoundCombatants(DataInputStream stream, Map<Long, User> users)
      throws IOException {
    int numActiveCombatants = stream.readUnsignedByte() + 1;
    List<CombatReportRoundCombatantDto> combatants = new ArrayList<>(numActiveCombatants);
    for (int i = 0; i < numActiveCombatants; i++) {
      combatants.add(loadRoundCombatant(stream, users));
    }
    return combatants;
  }

  private CombatReportRoundCombatantDto loadRoundCombatant(DataInputStream stream, Map<Long, User> users)
      throws IOException {
    long id = stream.readLong();
    User user = users.get(id);
    String name = user != null ? user.getName() : "[deleted]";
    Map<UnitKindDto, CombatReportRoundUnitGroupDto> unitGroups = loadRoundUnitGroups(stream);
    return new CombatReportRoundCombatantDto(name, unitGroups);
  }

  private Map<UnitKindDto, CombatReportRoundUnitGroupDto> loadRoundUnitGroups(DataInputStream stream)
      throws IOException {
    int numActiveGroups = stream.readUnsignedByte() + 1;
    Map<UnitKindDto, CombatReportRoundUnitGroupDto> groups = new EnumMap<>(UnitKindDto.class);
    for (int i = 0; i < numActiveGroups; i++) {
      UnitKind kind = UnitKind.values()[stream.readUnsignedByte()];
      long timesFired = stream.readLong();
      long timesWasShot = stream.readLong();
      long shieldDamageDealt = stream.readLong();
      long hullDamageDealt = stream.readLong();
      long shieldDamageTaken = stream.readLong();
      long hullDamageTaken = stream.readLong();
      long numRemainingUnits = stream.readLong();
      groups.put(Converter.convert(kind), new CombatReportRoundUnitGroupDto(numRemainingUnits, timesFired, timesWasShot,
          shieldDamageDealt, hullDamageDealt, shieldDamageTaken, hullDamageTaken));
    }
    return groups;
  }

  private Map<Long, User> findUsers(DataInputStream stream) throws IOException {
    Set<Long> ids = new HashSet<>();
    for (int i = 0; i < 2; i++) {
      int numCombatants = stream.readUnsignedByte() + 1;
      for (int j = 0; j < numCombatants; j++) {
        long id = stream.readLong();
        ids.add(id);

        // Skip coordinates.
        stream.skipBytes(4 + 4 + 4 + 1);

        // Skip technologies.
        stream.skipBytes(3);

        // Skip unit groups.
        int numGroups = stream.readUnsignedByte() + 1;
        stream.skipBytes(numGroups * 9);
      }
    }

    return userRepository.findByIdIn(ids).stream().collect(Collectors.toMap(User::getId, user -> user));
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "reportsSummaries", key = "#user.id")
  public void createSimplifiedCombatReport(User user, boolean isAttacker, Date at, User enemy, Coordinates coordinates,
                                           BattleResult result, int numRounds, Resources attackersLoss,
                                           Resources defendersLoss, Resources plunder, long debrisMetal,
                                           long debrisCrystal, double moonChance, boolean moonGiven,
                                           CombatReport combatReport) {
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

    SimplifiedCombatReport report = new SimplifiedCombatReport();
    report.setUser(user);
    report.setDeleted(false);
    report.setAt(at);
    report.setEnemyId(enemy.getId());
    report.setEnemyName(enemy.getName());
    report.setCoordinates(coordinates);
    report.setResult(res);
    report.setAttackersLoss(aLoss);
    report.setDefendersLoss(dLoss);
    report.setPlunder(plunder);
    report.setDebrisMetal(debrisMetal);
    report.setDebrisCrystal(debrisCrystal);
    report.setMoonChance(moonChance);
    report.setMoonGiven(moonGiven);
    if (!lostContact && combatReport != null) {
      report.setCombatReportId(combatReport.getId());
      report.setToken(combatReport.getToken());
    }
    simplifiedCombatReportRepository.save(report);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "reportsSummaries", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public List<SimplifiedCombatReportDto> getSimplifiedCombatReports(long bodyId,
                                                                    SimplifiedCombatReportSortOrderDto sortOrder,
                                                                    Sort.Direction direction, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    user.setCombatReportsSeenAt(Date.from(Instant.now()));

    List<SimplifiedCombatReport> reports = simplifiedCombatReportRepository.findReports(user,
        Converter.convert(sortOrder), direction, pageable);
    List<SimplifiedCombatReportDto> ret = new ArrayList<>(reports.size());
    for (SimplifiedCombatReport report : reports) {
      Long combatReportId = report.getCombatReportId();
      String token = null;
      if (combatReportId != null) {
        token = Base64.getUrlEncoder().withoutPadding().encodeToString(report.getToken());
      }
      ret.add(new SimplifiedCombatReportDto(report.getId(), report.getAt(), report.getEnemyId(), report.getEnemyName(),
          Converter.convert(report.getCoordinates()), Converter.convert(report.getResult()), report.getAttackersLoss(),
          report.getDefendersLoss(), Converter.convert(report.getPlunder()), report.getDebrisMetal(),
          report.getDebrisCrystal(), report.getMoonChance(), report.isMoonGiven(), combatReportId, token));
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
      throw new ReportDoesntExistException();
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
      throw new ReportDoesntExistException();
    }
    EspionageReport report = reportOptional.get();

    byte[] t = Base64.getUrlDecoder().decode(token);
    if (!MessageDigest.isEqual(t, report.getToken())) {
      logger.warn("Getting espionage report failed, wrong token: userId={} reportId={}", userId, id);
      throw new ReportDoesntExistException();
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
  @CacheEvict(cacheNames = "reportsSummaries", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
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
      throw new ReportDoesntExistException();
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
  @CacheEvict(cacheNames = "reportsSummaries", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
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
      throw new ReportDoesntExistException();
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
  @CacheEvict(cacheNames = "reportsSummaries", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
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
      throw new ReportDoesntExistException();
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
  @CacheEvict(cacheNames = "reportsSummaries", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
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
      throw new ReportDoesntExistException();
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
