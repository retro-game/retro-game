package com.github.retro_game.retro_game.security;

import com.github.retro_game.retro_game.cache.BodyInfoCache;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
  private final BodyInfoCache bodyInfoCache;

  public CustomPermissionEvaluator(BodyInfoCache bodyInfoCache) {
    this.bodyInfoCache = bodyInfoCache;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
    var user = (CustomUser) authentication.getPrincipal();
    var bodyId = (long) targetDomainObject;

    var perm = (String) permission;
    if (!"ACCESS".equals(perm)) {
      throw new IllegalArgumentException("Permission should be always 'ACCESS'");
    }

    var infoOpt = bodyInfoCache.find(bodyId);
    return infoOpt.isPresent() && infoOpt.get().getUserId() == user.getUserId();
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
                               Object permission) {
    return false;
  }
}
