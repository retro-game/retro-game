package com.github.retro_game.retro_game.config;

import com.github.retro_game.retro_game.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

@Configuration
public class LocaleConfig {
  @Bean
  public LocaleResolver localeResolver() {
    return new LocaleResolver() {
      @Autowired
      private UserService userService;

      @Override
      public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        return userService.getCurrentUserLocaleIfAuthenticated().orElse(Locale.ENGLISH);
      }

      @Override
      public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                            Locale locale) {
      }
    };
  }
}
