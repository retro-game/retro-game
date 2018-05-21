package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.Activity;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
class ActivityAdvice implements AfterReturningAdvice {
  private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
  private final ActivityService activityService;
  private final ActivityRequestCache activityRequestCache;

  public ActivityAdvice(ActivityService activityService, ActivityRequestCache activityRequestCache) {
    this.activityService = activityService;
    this.activityRequestCache = activityRequestCache;
  }

  @Override
  public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
    SpelExpressionParser parser = new SpelExpressionParser();
    MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(target, method, args,
        parameterNameDiscoverer);
    String[] bodies = AnnotationUtils.findAnnotation(method, Activity.class).bodies();
    for (String body : bodies) {
      long bodyId = parser.parseExpression(body).getValue(context, Long.class);
      if (activityRequestCache.addBody(bodyId)) {
        // The body activity wasn't updated in this request yet.
        activityService.handleBodyActivity(bodyId, null);
      }
    }

    long userId = CustomUser.getCurrentUserId();
    if (activityRequestCache.addUser(userId)) {
      // The user activity wasn't updated in this request yet.
      activityService.handleUserActivity(CustomUser.getCurrentUserId());
    }
  }
}