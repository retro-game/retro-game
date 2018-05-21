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
    if (permission instanceof String) {
      switch ((String) permission) {
        case "ACCESS_BODY": {
          long bodyId = (Long) targetDomainObject;
          CustomUser customUser = (CustomUser) authentication.getPrincipal();
          return bodyAccessPermissionEvaluator.hasAccessBodyPermission(customUser.getUserId(), bodyId);
        }
      }
    }

    return false;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
                               Object permission) {
    return false;
  }
}
