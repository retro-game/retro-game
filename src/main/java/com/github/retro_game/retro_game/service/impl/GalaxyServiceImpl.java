package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.cache.AllianceTagCache;
import com.github.retro_game.retro_game.cache.StatisticsCache;
import com.github.retro_game.retro_game.cache.UserAllianceCache;
import com.github.retro_game.retro_game.dto.ActiveStateDto;
import com.github.retro_game.retro_game.dto.GalaxySlotDto;
import com.github.retro_game.retro_game.dto.NoobProtectionRankDto;
import com.github.retro_game.retro_game.dto.StatisticsSummaryDto;
import com.github.retro_game.retro_game.entity.GalaxySlot;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.repository.GalaxySlotRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.ActivityService;
import com.github.retro_game.retro_game.service.GalaxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
class GalaxyServiceImpl implements GalaxyService {
  private static final Logger logger = LoggerFactory.getLogger(GalaxyServiceImpl.class);
  private final GalaxySlotRepository galaxySlotRepository;
  private final AllianceTagCache allianceTagCache;
  private final StatisticsCache statisticsCache;
  private final UserAllianceCache userAllianceCache;
  private ActivityService activityService;
  private NoobProtectionService noobProtectionService;
  private UserServiceInternal userServiceInternal;

  public GalaxyServiceImpl(GalaxySlotRepository galaxySlotRepository, AllianceTagCache allianceTagCache,
                           StatisticsCache statisticsCache, UserAllianceCache userAllianceCache) {
    this.galaxySlotRepository = galaxySlotRepository;
    this.allianceTagCache = allianceTagCache;
    this.statisticsCache = statisticsCache;
    this.userAllianceCache = userAllianceCache;
  }

  @Autowired
  public void setActivityService(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Autowired
  public void setNoobProtectionService(NoobProtectionService noobProtectionService) {
    this.noobProtectionService = noobProtectionService;
  }

  @Autowired
  public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
    this.userServiceInternal = userServiceInternal;
  }

  @Override
  public Map<Integer, GalaxySlotDto> getSlots(int galaxy, int system) {
    long userId = CustomUser.getCurrentUserId();
    logger.info("Viewing galaxy: userId={} galaxy={} system={}", userId, galaxy, system);

    long now = Instant.now().getEpochSecond();

    List<GalaxySlot> slots = galaxySlotRepository.findAllByGalaxyAndSystem(galaxy, system);

    // Get the activities of bodies.
    List<Long> ids = new ArrayList<>();
    for (GalaxySlot slot : slots) {
      ids.add(slot.getPlanetId());
      if (slot.getMoonId() != null) {
        ids.add(slot.getMoonId());
      }
    }
    Map<Long, Long> activities = activityService.getBodiesActivities(ids);

    Map<Integer, GalaxySlotDto> ret = new HashMap<>();
    for (GalaxySlot slot : slots) {
      boolean onVacation = slot.getVacationUntil() != null;
      boolean banned = userServiceInternal.isBanned(slot.getVacationUntil(), slot.isForcedVacation());
      NoobProtectionRankDto noobProtectionRank = noobProtectionService.getOtherPlayerRank(userId, slot.getUserId());

      boolean shortInactive = false;
      boolean longInactive = false;
      ActiveStateDto activeState = activityService.activeState(slot.getUserId());
      switch (activeState) {
        case INACTIVE_LONG:
          longInactive = true;
        case INACTIVE_SHORT:
          shortInactive = true;
          break;
        default:
          break;
      }

      StatisticsSummaryDto summary = statisticsCache.getUserSummary(slot.getUserId());
      int rank = summary == null ? 0 : summary.overall().rank();

      long activityAt = activities.getOrDefault(slot.getPlanetId(), 0L);
      if (slot.getMoonId() != null) {
        activityAt = Math.max(activityAt, activities.getOrDefault(slot.getMoonId(), 0L));
      }
      int activity = (int) ((now - activityAt) / 60L);
      if (activity < 15) {
        activity = 0;
      } else if (activity >= 60) {
        activity = 60;
      }

      var debrisMetal = slot.getDebrisMetal() != null ? slot.getDebrisMetal() : 0L;
      var debrisCrystal = slot.getDebrisCrystal() != null ? slot.getDebrisCrystal() : 0L;
      var recyclerCapacity = UnitItem.get(UnitKind.RECYCLER).getCapacity();
      var neededRecyclers = (int) Math.ceil((double) (debrisMetal + debrisCrystal) / recyclerCapacity);

      Long allianceId = userAllianceCache.getUserAlliance(slot.getUserId());
      String allianceTag = allianceId == null ? null : allianceTagCache.getTag(allianceId);

      boolean own = slot.getUserId() == userId;

      GalaxySlotDto s = new GalaxySlotDto(slot.getUserId(), slot.getUserName(), rank, onVacation, banned,
          noobProtectionRank, slot.getPlanetName(), Converter.convert(slot.getPlanetType()), slot.getPlanetImage(),
          slot.getMoonName(), slot.getMoonImage(), activity, debrisMetal, debrisCrystal, neededRecyclers, allianceId,
          allianceTag, own, shortInactive, longInactive);
      ret.put(slot.getPosition(), s);
    }
    return ret;
  }

  @Override
  public Map<Integer, GalaxySlotDto> getSlots(long bodyId, int galaxy, int system) {
    return getSlots(galaxy, system);
  }
}
