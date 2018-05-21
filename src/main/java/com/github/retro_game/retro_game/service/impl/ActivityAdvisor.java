package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.service.Activity;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
class ActivityAdvisor extends AbstractPointcutAdvisor {
  private final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
      return AnnotationUtils.findAnnotation(method, Activity.class) != null;
    }
  };

  private final ActivityAdvice activityAdvice;

  public ActivityAdvisor(ActivityAdvice activityAdvice) {
    this.activityAdvice = activityAdvice;
  }

  @Override
  public Pointcut getPointcut() {
    return this.pointcut;
  }

  @Override
  public Advice getAdvice() {
    return this.activityAdvice;
  }
}
