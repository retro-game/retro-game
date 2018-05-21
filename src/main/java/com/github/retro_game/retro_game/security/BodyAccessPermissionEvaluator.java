package com.github.retro_game.retro_game.security;

import com.github.retro_game.retro_game.model.repository.BodyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
class BodyAccessPermissionEvaluator {
  private final BodyRepository bodyRepository;

  public BodyAccessPermissionEvaluator(BodyRepository bodyRepository) {
    this.bodyRepository = bodyRepository;
  }

  @Cacheable("userBodiesIds")
  public boolean hasAccessBodyPermission(long userId, long bodyId) {
    return bodyRepository.existsByIdAndUser_Id(bodyId, userId);
  }
}
