package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.cache.UserInfoCache;
import com.github.retro_game.retro_game.dto.CombatReportSortOrderDto;
import com.github.retro_game.retro_game.service.HallOfFameService;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class HallOfFameController {
  private final boolean hallOfFameEnabled;
  private final UserInfoCache userInfoCache;
  private final HallOfFameService hallOfFameService;
  private final UserService userService;

  public HallOfFameController(@Value("${retro-game.hall-of-fame-enabled}") boolean hallOfFameEnabled,
                              UserInfoCache userInfoCache, HallOfFameService hallOfFameService,
                              UserService userService) {
    this.hallOfFameEnabled = hallOfFameEnabled;
    this.userInfoCache = userInfoCache;
    this.hallOfFameService = hallOfFameService;
    this.userService = userService;
  }

  @GetMapping("/hall-of-fame")
  public String hallOfFame(@RequestParam(name = "body") long bodyId,
                           @RequestParam(required = false) CombatReportSortOrderDto order, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("enabled", hallOfFameEnabled);

    if (!hallOfFameEnabled) {
      return "hall-of-fame";
    }

    if (order == null) {
      order = CombatReportSortOrderDto.LOSS;
    }
    var entries = hallOfFameService.get(order);

    var userIds = new HashSet<Long>();
    for (var entry : entries) {
      userIds.addAll(entry.attackers());
      userIds.addAll(entry.defenders());
    }
    var userInfos = userInfoCache.getAll(userIds);

    Function<ArrayList<Long>, String> makeNames = list -> list.stream().map(id -> {
      var info = userInfos.get(id);
      return info == null ? "[deleted]" : info.getName();
    }).collect(Collectors.joining(", "));

    var attackerNames = new ArrayList<String>(entries.size());
    var defenderNames = new ArrayList<String>(entries.size());
    for (var entry : entries) {
      attackerNames.add(makeNames.apply(entry.attackers()));
      defenderNames.add(makeNames.apply(entry.defenders()));
    }

    model.addAttribute("entries", entries);
    model.addAttribute("attackerNames", attackerNames);
    model.addAttribute("defenderNames", defenderNames);

    return "hall-of-fame";
  }
}
