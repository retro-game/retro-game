package com.github.retro_game.retro_game.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
  private final BodyAccessPermissionEvaluator bodyAccessPermissionEvaluator;

  public CustomPermissionEvaluator(BodyAccessPermissionEvaluator bodyAccessPermissionEvaluator) {
    this.bodyAccessPermissionEvaluator = bodyAccessPermissionEvaluator;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
    var bodyId = (long) targetDomainObject;

    var perm = (String) permission;
    if (!"ACCESS".equals(perm)) {
      throw new IllegalArgumentException("Permission should be always 'ACCESS'");
    }

    var user = (CustomUser) authentication.getPrincipal();
    return bodyAccessPermissionEvaluator.hasAccessBodyPermission(user.getUserId(), bodyId);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
                               Object permission) {
    return false;
  }
}
