package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.cache.UserInfoCache;
import com.github.retro_game.retro_game.dto.CombatReportCombatantDto;
import com.github.retro_game.retro_game.dto.CombatReportDto;
import com.github.retro_game.retro_game.service.CombatReportService;
import com.github.retro_game.retro_game.service.exception.ReportDoesNotExistException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class CombatReportController {
  private final UserInfoCache userInfoCache;
  private final CombatReportService combatReportService;

  public CombatReportController(UserInfoCache userInfoCache, CombatReportService combatReportService) {
    this.userInfoCache = userInfoCache;
    this.combatReportService = combatReportService;
  }

  @GetMapping("/combat-report")
  public String combatReport(@RequestParam UUID id, Model model) {
    CombatReportDto report = null;
    try {
      report = combatReportService.get(id);
    } catch (ReportDoesNotExistException ignored) {
    }
    model.addAttribute("report", report);

    if (report != null) {
      var userIds =
          Stream.concat(report.attackers().stream(), report.defenders().stream()).map(CombatReportCombatantDto::userId)
              .collect(Collectors.toSet());
      var userInfos = userInfoCache.getAll(userIds);
      model.addAttribute("userInfos", userInfos);
    }

    return "combat-report";
  }
}
