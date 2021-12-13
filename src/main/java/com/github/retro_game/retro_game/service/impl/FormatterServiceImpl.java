package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.service.FormatterService;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service("formatterService")
class FormatterServiceImpl implements FormatterService {
  private final MessageSource messageSource;
  private UserService userService;

  FormatterServiceImpl(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  @Override
  public String formatTime(long s) {
    long seconds = s % 60;
    s /= 60;
    long minutes = s % 60;
    s /= 60;
    long hours = s % 24;
    long days = s / 24;
    return (days > 0 ? String.valueOf(days) + " d " : "") + String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }

  @Override
  public String formatTimeMs(long ms) {
    var milliseconds = ms % 1000;
    ms /= 1000;
    var seconds = ms % 60;
    ms /= 60;
    var minutes = ms % 60;
    ms /= 60;
    var hours = ms % 24;
    var days = ms / 24;
    return (days > 0 ? days + " d " : "") + String.format("%02d:%02d:%02d", hours, minutes, seconds) +
        (milliseconds > 0 ? String.format(".%03d", milliseconds) : "");
  }

  @Override
  public String prefixedNumber(long i) {
    double n = i;
    char suffix;
    if (i >= 1e12) {
      suffix = 'T';
      n /= 1e12;
    } else if (i >= 1e9) {
      suffix = 'G';
      n /= 1e9;
    } else if (i >= 1e6) {
      suffix = 'M';
      n /= 1e6;
    } else if (i >= 1e3) {
      suffix = 'k';
      n /= 1e3;
    } else {
      return String.valueOf(i);
    }
    i = Math.round(10.0 * n);
    return String.format("%d.%d%c", i / 10, i % 10, suffix);
  }

  @Override
  public String formatUnits(Map<UnitKindDto, Integer> units) {
    return units.entrySet().stream()
        .map(entry -> {
          String msgKey = String.format("items.%s.abbreviation", entry.getKey());
          Locale locale = userService.getCurrentUserLocaleIfAuthenticated().orElse(Locale.getDefault());
          String msg = messageSource.getMessage(msgKey, null, locale);
          return entry.getValue() + " " + msg;
        })
        .collect(Collectors.joining(", "));
  }
}
