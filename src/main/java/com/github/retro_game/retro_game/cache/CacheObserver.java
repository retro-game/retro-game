package com.github.retro_game.retro_game.cache;

import org.springframework.stereotype.Component;

@Component
public class CacheObserver {
  private final BodyInfoCache bodyInfoCache;
  private final UserBodiesCache userBodiesCache;

  public CacheObserver(BodyInfoCache bodyInfoCache, UserBodiesCache userBodiesCache) {
    this.bodyInfoCache = bodyInfoCache;
    this.userBodiesCache = userBodiesCache;
  }

  public void notifyBodyCreated(long userId) {
    // The mapping user -> list of bodies must be refreshed. The cache for body info for the newly created body should
    // be empty.
    userBodiesCache.evict(userId);
  }

  public void notifyBodyUpdated(long bodyId) {
    // We need to refresh the given body. The mapping user -> list of bodies should stay unchanged.
    bodyInfoCache.evict(bodyId);
  }

  public void notifyBodyDeleted(long userId, long bodyId) {
    // The mapping user -> list of bodies must be updated, as the body is deleted. There is also no reason to keep the
    // body info cache for the given body, since it won't be used anymore.
    bodyInfoCache.evict(bodyId);
    userBodiesCache.evict(userId);
  }
}
