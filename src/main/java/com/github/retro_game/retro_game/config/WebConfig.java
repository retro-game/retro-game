package com.github.retro_game.retro_game.config;

import java.util.List;

import com.github.retro_game.retro_game.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.Nullable;
import org.springframework.mobile.device.DeviceHandlerMethodArgumentResolver;
import org.springframework.mobile.device.DeviceResolverHandlerInterceptor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Configuration
class WebConfig extends DelegatingWebMvcConfiguration {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/static/**")
        .addResourceLocations("classpath:/public/static/")
        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
  }

  @Bean
  @Override
  public LocaleResolver localeResolver() {
    return new LocaleResolver() {
      @Autowired
      private UserService userService;

      @Override
      public Locale resolveLocale(HttpServletRequest request) {
        return userService.getCurrentUserLocaleIfAuthenticated().orElse(Locale.ENGLISH);
      }

      @Override
      public void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response,
                            @Nullable Locale locale) {
      }
    };
  }

  @Bean
  public DeviceResolverHandlerInterceptor deviceResolverHandlerInterceptor() { 
      return new DeviceResolverHandlerInterceptor(); 
  }

  @Bean
  public DeviceHandlerMethodArgumentResolver deviceHandlerMethodArgumentResolver() { 
      return new DeviceHandlerMethodArgumentResolver(); 
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) { 
      registry.addInterceptor(deviceResolverHandlerInterceptor()); 
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
      argumentResolvers.add(deviceHandlerMethodArgumentResolver()); 
  }
}
