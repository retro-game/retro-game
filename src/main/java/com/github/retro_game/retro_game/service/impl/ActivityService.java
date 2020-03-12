package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.ActiveStateDto;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

interface ActivityService {
  // Updates the body's activity if necessary. If 'at' param is null, current time is taken.
  void handleBodyActivity(long bodyId, @Nullable Long at);

  // Updates the user's activity. This method may also update the homeworld's activity, when it is called for the first
  // time after 3 AM.
  void handleUserActivity(long userId);

  @Nullable
  Long getBodyActivity(long bodyId);

  Map<Long, Long> getBodiesActivities(List<Long> bodiesIds);

  ActiveStateDto activeState(long userId);

  boolean isInactive(long userId);
}
