package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.GalaxySlot;
import com.github.retro_game.retro_game.model.repository.GalaxySlotRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.GalaxyService;
import com.github.retro_game.retro_game.service.dto.GalaxySlotDto;
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
  private ActivityService activityService;

  public GalaxyServiceImpl(GalaxySlotRepository galaxySlotRepository) {
    this.galaxySlotRepository = galaxySlotRepository;
  }

  @Autowired
  public void setActivityService(ActivityService activityService) {
    this.activityService = activityService;
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

      boolean own = slot.getUserId() == userId;

      GalaxySlotDto s = new GalaxySlotDto(slot.getUserId(), slot.getUserName(), slot.getPlanetName(),
          Converter.convert(slot.getPlanetType()), slot.getPlanetImage(), slot.getMoonName(), slot.getMoonImage(),
          activity, slot.getDebrisMetal(), slot.getDebrisCrystal(), own);
      ret.put(slot.getPosition(), s);
    }
    return ret;
  }

  @Override
  public Map<Integer, GalaxySlotDto> getSlots(long bodyId, int galaxy, int system) {
    return getSlots(galaxy, system);
  }
}
