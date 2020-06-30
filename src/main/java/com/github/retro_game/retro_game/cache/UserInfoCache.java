package com.github.retro_game.retro_game.cache;

import com.github.retro_game.retro_game.dto.UserInfoDto;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Component
public class UserInfoCache {
  private final static int MAX_SIZE = 4096;

  // Guava provides a thread-safe implementation, no synchronization is required.
  private final LoadingCache<Long, UserInfoDto> cache;

  public UserInfoCache(BodyRepository bodyRepository, UserRepository userRepository) {
    cache = CacheBuilder.newBuilder()
        .maximumSize(MAX_SIZE)
        .build(new CacheLoader<Long, UserInfoDto>() {
          @Override
          @ParametersAreNonnullByDefault
          public UserInfoDto load(Long userId) {
            var user = userRepository.getOne(userId);
            var bodiesIds = bodyRepository.findIdsByUserIdOrderById(userId);
            return new UserInfoDto(userId, user.getName(), bodiesIds);
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

  public void evict(long userId) {
    cache.invalidate(userId);
  }
}
