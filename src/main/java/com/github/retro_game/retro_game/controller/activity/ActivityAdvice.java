package com.github.retro_game.retro_game.controller.activity;

import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.ActivityService;
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

  ActivityAdvice(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
    var parser = new SpelExpressionParser();
    var context = new MethodBasedEvaluationContext(target, method, args, parameterNameDiscoverer);
    var bodies = AnnotationUtils.findAnnotation(method, Activity.class).bodies();
    for (var body : bodies) {
      var bodyId = parser.parseExpression(body).getValue(context, Long.class);
      activityService.handleBodyActivity(bodyId, null);
    }

    // The annotation is meant to update the current user activity.
    var userId = CustomUser.getCurrentUserId();
    activityService.handleUserActivity(userId);
  }
}
