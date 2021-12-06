package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.ItemCostUtils;
import com.github.retro_game.retro_game.model.ItemTimeUtils;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.DetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
class DetailsServiceImpl implements DetailsService {
  private final int buildingQueueCapacity;
  private final ItemTimeUtils itemTimeUtils;
  private final UserRepository userRepository;
  private BodyServiceInternal bodyServiceInternal;
  private BuildingsServiceInternal buildingsServiceInternal;
  private UnitService unitService;

  public DetailsServiceImpl(@Value("${retro-game.building-queue-capacity}") int buildingQueueCapacity,
                            ItemTimeUtils itemTimeUtils, UserRepository userRepository) {
    this.buildingQueueCapacity = buildingQueueCapacity;
    this.itemTimeUtils = itemTimeUtils;
    this.userRepository = userRepository;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Autowired
  public void setBuildingsServiceInternal(BuildingsServiceInternal buildingsServiceInternal) {
    this.buildingsServiceInternal = buildingsServiceInternal;
  }

  @Autowired
  public void setUnitService(UnitService unitService) {
    this.unitService = unitService;
  }

  @PostConstruct
  private void checkProperties() {
    Assert.isTrue(buildingQueueCapacity >= 1,
        "retro-game.building-queue-capacity must be at least 1");
  }

  @Override
  public BuildingDetailsDto getBuildingDetails(long bodyId, BuildingKindDto kind) {
    Body body = bodyServiceInternal.getUpdated(bodyId);

    BuildingKind k = Converter.convert(kind);

    Collection<BuildingQueueEntry> queue = body.getBuildingQueue().values();

    var currentLevel = body.getBuildingLevel(k);
    var futureLevel = currentLevel;
    for (BuildingQueueEntry entry : queue) {
      if (entry.kind() == k) {
        assert entry.action() == BuildingQueueAction.CONSTRUCT || entry.action() == BuildingQueueAction.DESTROY;
        futureLevel += entry.action() == BuildingQueueAction.CONSTRUCT ? 1 : -1;
      }
    }

    ResourcesDto destructionCost = null;
    long destructionTime = 0;

    boolean destroyable = k != BuildingKind.TERRAFORMER && k != BuildingKind.LUNAR_BASE && futureLevel >= 1;
    boolean canDestroyNow = false;

    if (destroyable) {
      var cost = ItemCostUtils.getCost(k, futureLevel - 1);
      destructionCost = Converter.convert(cost);

      var roboticsFactoryLevel = body.getBuildingLevel(BuildingKind.ROBOTICS_FACTORY);
      var naniteFactoryLevel = body.getBuildingLevel(BuildingKind.NANITE_FACTORY);
      destructionTime = itemTimeUtils.getBuildingDestructionTime(cost, roboticsFactoryLevel, naniteFactoryLevel);

      if (queue.size() < buildingQueueCapacity && (!queue.isEmpty() || body.getResources().greaterOrEqual(cost))) {
        canDestroyNow = true;
      }
    }

    return new BuildingDetailsDto(currentLevel, futureLevel, destructionCost, destructionTime, destroyable,
        canDestroyNow);
  }

  @Override
  public TechnologyDetailsDto getTechnologyDetails(long bodyId, TechnologyKindDto kind) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    TechnologyKind k = Converter.convert(kind);

    int currentLevel = user.getTechnologyLevel(k);
    int futureLevel = currentLevel + (int) user.getTechnologyQueue().values().stream()
        .filter(e -> e.getKind() == k)
        .count();

    return new TechnologyDetailsDto(currentLevel, futureLevel);
  }

  @Override
  public UnitDetailsDto getUnitDetails(long bodyId, UnitKindDto kind) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    UnitKind k = Converter.convert(kind);
    UnitItem item = UnitItem.getAll().get(k);

    double weapons = unitService.getWeapons(k, user);
    double shield = unitService.getShield(k, user);
    double armor = unitService.getArmor(k, user);

    Map<UnitKindDto, Integer> rapidFireAgainst = item.getRapidFireAgainst().entrySet().stream()
        .collect(Collectors.toMap(entry -> Converter.convert(entry.getKey()), Map.Entry::getValue,
            (l, r) -> {
              throw new IllegalStateException();
            }, () -> new EnumMap<>(UnitKindDto.class)));

    Map<UnitKindDto, Integer> rapidFireFrom = UnitItem.getAll().entrySet().stream()
        .filter(entry -> entry.getValue().getRapidFireAgainst().containsKey(k))
        .collect(Collectors.toMap(entry -> Converter.convert(entry.getKey()),
            entry -> entry.getValue().getRapidFireAgainst().get(k),
            (l, r) -> {
              throw new IllegalStateException();
            }, () -> new EnumMap<>(UnitKindDto.class)));

    return new UnitDetailsDto(weapons, shield, armor, item.getCapacity(), item.getConsumption(user),
        unitService.getSpeed(k, user), item.getBaseWeapons(), item.getBaseShield(), item.getBaseArmor(),
        item.getBaseSpeed(user), rapidFireAgainst, rapidFireFrom);
  }
}
