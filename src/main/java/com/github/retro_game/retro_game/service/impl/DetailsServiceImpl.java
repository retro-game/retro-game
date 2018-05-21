package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.*;
import com.github.retro_game.retro_game.model.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.DetailsService;
import com.github.retro_game.retro_game.service.dto.*;
import com.github.retro_game.retro_game.service.impl.item.unit.UnitItem;
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
  private final UserRepository userRepository;
  private BodyServiceInternal bodyServiceInternal;
  private BuildingsServiceInternal buildingsServiceInternal;
  private UnitService unitService;

  public DetailsServiceImpl(@Value("${retro-game.building-queue-capacity}") int buildingQueueCapacity,
                            UserRepository userRepository) {
    this.buildingQueueCapacity = buildingQueueCapacity;
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

    Building building = body.getBuildings().get(k);
    int currentLevel = building != null ? building.getLevel() : 0;
    int futureLevel = currentLevel;
    for (BuildingQueueEntry entry : queue) {
      if (entry.getKind() == k) {
        assert entry.getAction() == BuildingQueueAction.CONSTRUCT || entry.getAction() == BuildingQueueAction.DESTROY;
        futureLevel += entry.getAction() == BuildingQueueAction.CONSTRUCT ? 1 : -1;
      }
    }

    ResourcesDto destructionCost = null;
    long destructionTime = 0;

    boolean destroyable = k != BuildingKind.TERRAFORMER && k != BuildingKind.LUNAR_BASE && futureLevel >= 1;
    boolean canDestroyNow = false;

    if (destroyable) {
      Resources cost = buildingsServiceInternal.getCost(k, futureLevel - 1);
      destructionCost = Converter.convert(cost);
      destructionTime = buildingsServiceInternal.getDestructionTime(cost, body);

      if (queue.size() < buildingQueueCapacity && (!queue.isEmpty() || body.getResources().greaterOrEqual(cost))) {
        canDestroyNow = true;
      }
    }

    return new BuildingDetailsDto(currentLevel, futureLevel, destructionCost, destructionTime, destroyable,
        canDestroyNow);
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
