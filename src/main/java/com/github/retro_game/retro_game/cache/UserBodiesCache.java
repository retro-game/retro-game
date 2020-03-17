package com.github.retro_game.retro_game.cache;

import com.github.retro_game.retro_game.repository.BodyRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Component
public class UserBodiesCache {
  private final static int MAX_SIZE = 4096;

  // Guava provides a thread-safe implementation, no synchronization is required.
  // The IDs are kept ordered.
  private final LoadingCache<Long, List<Long>> cache;

  public UserBodiesCache(BodyRepository bodyRepository) {
    cache = CacheBuilder.newBuilder()
        .maximumSize(MAX_SIZE)
        .build(new CacheLoader<>() {
                 @Override
                 @ParametersAreNonnullByDefault
                 public List<Long> load(Long userId) {
                   return bodyRepository.findIdsByUserIdOrderById(userId);
                 }
               }
        );
  }

  // Gets the list of IDs of bodies for the given user. The returned list is sorted in ascending order.
  public List<Long> get(long userId) {
    return cache.getUnchecked(userId);
  }

  public void evict(long userId) {
    cache.invalidate(userId);
  }
}
