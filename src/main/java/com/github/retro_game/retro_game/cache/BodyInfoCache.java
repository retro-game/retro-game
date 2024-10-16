package com.github.retro_game.retro_game.cache;

import com.github.retro_game.retro_game.dto.BodyInfoDto;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.service.impl.Converter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class BodyInfoCache {
  private final static int MAX_SIZE = 4096;

  // Guava provides a thread-safe implementation, no synchronization is required.
  private final LoadingCache<Long, BodyInfoDto> cache;

  public BodyInfoCache(BodyRepository bodyRepository) {
    cache = CacheBuilder.newBuilder()
        .maximumSize(MAX_SIZE)
        .build(new CacheLoader<>() {
                 @Override
                 @ParametersAreNonnullByDefault
                 public BodyInfoDto load(Long bodyId) {
                   var body = bodyRepository.getOne(bodyId);
                   // TODO: This will load the user, which is unnecessary. We could probably keep only the id of the
                   // owner in the body entity.
                   var userId = body.getUser().getId();
                   var coords = Converter.convert(body.getCoordinates());
                   return new BodyInfoDto(bodyId, userId, body.getName(), coords);
                 }
               }
        );
  }

  public BodyInfoDto get(long bodyId) {
    return cache.getUnchecked(bodyId);
  }

  public Optional<BodyInfoDto> find(long bodyId) {
    try {
      return Optional.of(get(bodyId));
    } catch (UncheckedExecutionException e) {
      if (e.getCause() instanceof EntityNotFoundException) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public ImmutableMap<Long, BodyInfoDto> getAll(Iterable<Long> bodiesIds) {
    try {
      return cache.getAll(bodiesIds);
    } catch (ExecutionException e) {
      throw new UncheckedExecutionException(e);
    }
  }

  public void evict(long bodyId) {
    cache.invalidate(bodyId);
  }
}
