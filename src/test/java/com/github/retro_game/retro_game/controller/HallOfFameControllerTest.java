package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.cache.UserInfoCache;
import com.github.retro_game.retro_game.dto.BattleResultDto;
import com.github.retro_game.retro_game.dto.HallOfFameEntryDto;
import com.github.retro_game.retro_game.dto.ResourcesDto;
import com.github.retro_game.retro_game.dto.UserInfoDto;
import com.github.retro_game.retro_game.service.HallOfFameService;
import com.github.retro_game.retro_game.service.UserService;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.ui.ExtendedModelMap;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class HallOfFameControllerTest {
  @After
  public void resetLocale() {
    LocaleContextHolder.resetLocaleContext();
  }

  @Test
  public void hallOfFameNamesNpcCombatantsWithoutLoadingThemFromUserCache() {
    // StaticMessageSource has no en_US -> en fallback, so the locale the controller resolves must
    // match the one the messages are registered under, regardless of the machine's default locale.
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    var userInfoCache = new CapturingUserInfoCache(ImmutableMap.of(
        7L, new UserInfoDto(7, "Player", List.of()),
        8L, new UserInfoDto(8, "Rival", List.of())
    ));
    var messageSource = new StaticMessageSource();
    var model = new ExtendedModelMap();
    var entry = new HallOfFameEntryDto(
        1,
        new ArrayList<>(List.of(7L, -2L)),
        new ArrayList<>(List.of(-1L, 8L)),
        BattleResultDto.DRAW,
        100,
        200,
        new ResourcesDto(0, 0, 0),
        new ResourcesDto(0, 0, 0),
        UUID.randomUUID()
    );
    HallOfFameService hallOfFameService = sortOrder -> new ArrayList<>(List.of(entry));
    var userService = userService();
    var controller = new HallOfFameController(true, userInfoCache, hallOfFameService, messageSource, userService);
    messageSource.addMessage("combatantPirates", Locale.ENGLISH, "Pirates");
    messageSource.addMessage("combatantAliens", Locale.ENGLISH, "Aliens");

    var view = controller.hallOfFame(1, null, model);

    assertThat(view).isEqualTo("hall-of-fame");
    assertThat(model.getAttribute("attackerNames")).isEqualTo(List.of("Player, Aliens"));
    assertThat(model.getAttribute("defenderNames")).isEqualTo(List.of("Pirates, Rival"));

    assertThat(StreamSupport.stream(userInfoCache.requestedUserIds.spliterator(), false).toList())
        .containsExactlyInAnyOrder(7L, 8L);
  }

  private static UserService userService() {
    return (UserService) Proxy.newProxyInstance(
        UserService.class.getClassLoader(),
        new Class<?>[]{UserService.class},
        (proxy, method, args) -> {
          if (method.getReturnType().equals(boolean.class)) {
            return false;
          }
          if (method.getReturnType().equals(long.class)) {
            return 0L;
          }
          return null;
        });
  }

  private static class CapturingUserInfoCache extends UserInfoCache {
    private final ImmutableMap<Long, UserInfoDto> userInfos;
    private Iterable<Long> requestedUserIds = Collections.emptyList();

    private CapturingUserInfoCache(ImmutableMap<Long, UserInfoDto> userInfos) {
      super(null, null);
      this.userInfos = userInfos;
    }

    @Override
    public ImmutableMap<Long, UserInfoDto> getAll(Iterable<Long> userIds) {
      requestedUserIds = userIds;
      return userInfos;
    }
  }
}
