package com.github.retro_game.retro_game.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashSet;
import java.util.Set;

// Storage for caching whether the activity of a body or user was already updated in the same request. Using this class
// can avoid unnecessary request to the redis server.
@Component
@RequestScope
class ActivityRequestCache {
  private final Set<Long> bodiesIds = new HashSet<>();
  private final Set<Long> usersIds = new HashSet<>();

  boolean addBody(long bodyId) {
    return bodiesIds.add(bodyId);
  }

  boolean addUser(long userId) {
    return usersIds.add(userId);
  }
}
