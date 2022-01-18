package com.github.retro_game.retro_game.cache;

import com.github.retro_game.retro_game.dto.UserInfoDto;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class UserInfoCache {
  private final static int MAX_SIZE = 4096;

  // Guava provides a thread-safe implementation, no synchronization is required.
  private final LoadingCache<Long, UserInfoDto> cache;

  public UserInfoCache(BodyRepository bodyRepository, UserRepository userRepository) {
    cache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build(new CacheLoader<>() {
      @Override
      @ParametersAreNonnullByDefault
      public UserInfoDto load(Long userId) {
        var user = userRepository.getOne(userId);
        var bodiesIds = bodyRepository.findIdsByUserIdOrderById(userId);
        return new UserInfoDto(userId, user.getName(), bodiesIds);
      }

      @Override
      @ParametersAreNonnullByDefault
      public Map<Long, UserInfoDto> loadAll(Iterable<? extends Long> userIds) {
        var map = new HashMap<Long, UserInfoDto>();
        var ids = new ArrayList<Long>();
        userIds.iterator().forEachRemaining(ids::add);
        var users = userRepository.findByIdIn(ids);
        for (var user : users) {
          var userId = user.getId();
          // TODO: We could perform a single query.
          var bodiesIds = bodyRepository.findIdsByUserIdOrderById(userId);
          var info = new UserInfoDto(userId, user.getName(), bodiesIds);
          map.put(userId, info);
        }
        return map;
      }
    });
  }

  public UserInfoDto get(long userId) {
    return cache.getUnchecked(userId);
  }

  public Optional<UserInfoDto> find(long userId) {
    try {
      return Optional.of(get(userId));
    } catch (UncheckedExecutionException e) {
      if (e.getCause() instanceof EntityNotFoundException) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public ImmutableMap<Long, UserInfoDto> getAll(Iterable<Long> userIds) {
    try {
      return cache.getAll(userIds);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public void evict(long userId) {
    cache.invalidate(userId);
  }
}
