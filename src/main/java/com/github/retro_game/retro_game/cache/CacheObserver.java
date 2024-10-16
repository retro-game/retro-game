package com.github.retro_game.retro_game.cache;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class CacheObserver {
  private final BodyInfoCache bodyInfoCache;
  private final UserInfoCache userInfoCache;

  public CacheObserver(BodyInfoCache bodyInfoCache, UserInfoCache userInfoCache) {
    this.bodyInfoCache = bodyInfoCache;
    this.userInfoCache = userInfoCache;
  }

  public void notifyBodyCreated(long userId) {
    // The mapping user -> list of bodies in user info must be refreshed. The cache for body info for the newly created
    // body should be empty.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        userInfoCache.evict(userId);
      }
    });
  }

  public void notifyBodyUpdated(long bodyId) {
    // We need to refresh the given body. The mapping user -> list of bodies in user info should stay unchanged.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        bodyInfoCache.evict(bodyId);
      }
    });
  }

  public void notifyBodyDeleted(long userId, long bodyId) {
    // The mapping user -> list of bodies in user info must be updated, as the body is deleted. There is also no reason
    // to keep the body info cache for the given body, since it won't be used anymore.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        bodyInfoCache.evict(bodyId);
        userInfoCache.evict(userId);
      }
    });
  }
}
