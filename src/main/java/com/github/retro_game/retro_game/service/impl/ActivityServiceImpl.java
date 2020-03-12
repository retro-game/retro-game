package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.ActiveStateDto;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
class ActivityServiceImpl implements ActivityService {
  private final static String bodyPrefix = "activity_body";
  private final static String userPrefix = "activity_user";
  private final static Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
  private final UserRepository userRepository;
  private final int numberOfDaysForShortInactive;
  private final int numberOfDaysForLongInactive;

  @Resource(name = "redisTemplate")
  private ValueOperations<String, String> valueOperations;

  public ActivityServiceImpl(UserRepository userRepository,
                             @Value("${retro-game.short-inactive-number-of-days}") int numberOfDaysForShortInactive,
                             @Value("${retro-game.long-inactive-number-of-days}") int numberOfDaysForLongInactive) {
    this.userRepository = userRepository;
    this.numberOfDaysForShortInactive = numberOfDaysForShortInactive;
    this.numberOfDaysForLongInactive = numberOfDaysForLongInactive;
  }

  @Override
  public void handleBodyActivity(long bodyId, @Nullable Long at) {
    String key = String.format("%s_%d", bodyPrefix, bodyId);

    if (at == null) {
      at = Instant.now().getEpochSecond();
    } else {
      // The activity may have already been updated with greater time. This can happen when the scheduler is lagging and
      // it processes some old events. For example, an event should be processed at time T, a user clicks at time T+1
      // (updates the activity to T+1), then the scheduler starts to process the event at time T+2 and tries to set the
      // activity to T. Without this check, this would overwrite the activity incorrectly.
      Long cur = getBodyActivity(bodyId);
      if (cur != null && cur >= at) {
        return;
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Handling body activity: userId={} at={}", bodyId, Date.from(Instant.ofEpochSecond(at)));
    }

    valueOperations.set(key, String.valueOf(at));
  }

  @Override
  public void handleUserActivity(long userId) {
    logger.debug("Handling user activity: userId={}", userId);

    String key = String.format("%s_%d", userPrefix, userId);
    long now = Instant.now().getEpochSecond();
    String prev = valueOperations.getAndSet(key, String.valueOf(now));

    if (prev != null) {
      long lastActivity = Long.valueOf(prev);
      if (lastActivity > last3am()) {
        // Previously the user clicked after the last 3 AM, thus we don't need to update the activity on the homeworld.
        return;
      }
    }

    // We need to set activity on the homeworld, as there is no previous user activity or the user clicked after 3 AM
    // for the first time.
    User user = userRepository.getOne(userId);
    Optional<Long> homeworldId = user.getBodies().keySet().stream().min(Long::compareTo);
    if (!homeworldId.isPresent()) {
      logger.error("Updating user activity failed, a homeworld is not present: userId={}", userId);
    } else {
      logger.info("Updating homeworld activity: userId={}", userId);
      handleBodyActivity(homeworldId.get(), now);
    }
  }

  private long last3am() {
    LocalDateTime today3am = LocalDate.now().atTime(3, 0);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime last3am = today3am.isAfter(now) ? today3am.minusDays(1) : today3am;
    return last3am.atZone(ZoneId.systemDefault()).toEpochSecond();
  }

  @Override
  public Long getBodyActivity(long bodyId) {
    String key = String.format("%s_%d", bodyPrefix, bodyId);
    String s = valueOperations.get(key);
    return s != null ? Long.valueOf(s) : null;
  }

  @Override
  public Map<Long, Long> getBodiesActivities(List<Long> bodiesIds) {
    List<String> keys = bodiesIds.stream()
        .map(id -> String.format("%s_%d", bodyPrefix, id))
        .collect(Collectors.toList());
    List<String> activities = valueOperations.multiGet(keys);
    Map<Long, Long> ret = new HashMap<>();
    Iterator<Long> idsIt = bodiesIds.iterator();
    Iterator<String> activitiesIt = activities.iterator();
    while (idsIt.hasNext() && activitiesIt.hasNext()) {
      Long id = idsIt.next();
      String s = activitiesIt.next();
      if (s != null) {
        ret.put(id, Long.valueOf(s));
      }
    }
    return ret;
  }

  @Override
  public ActiveStateDto activeState(long userId) {
    long numberOfInactiveDays = this.numberOfInactiveDays(userId);
    if (numberOfInactiveDays >= numberOfDaysForLongInactive)
      return ActiveStateDto.INACTIVE_LONG;
    if (numberOfInactiveDays >= numberOfDaysForShortInactive)
      return ActiveStateDto.INACTIVE_SHORT;

    return ActiveStateDto.ACTIVE;
  }

  @Override
  public boolean isInactive(long userId) {
    ActiveStateDto activeState = this.activeState(userId);
    return activeState == ActiveStateDto.INACTIVE_LONG || activeState == ActiveStateDto.INACTIVE_SHORT;
  }

  private long numberOfInactiveDays(long userId) {
    String key = String.format("%s_%d", userPrefix, userId);
    String lastActivity = valueOperations.get(key);

    if (lastActivity == null)
      return -1;

    LocalDateTime lastActivityDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(lastActivity)), ZoneId.systemDefault());
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

    return lastActivityDate.until(now, ChronoUnit.DAYS);
  }
}
