package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.StatisticsKindDto;
import com.github.retro_game.retro_game.service.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;

@Controller
@Validated
public class RankingController {
  private final StatisticsService statisticsService;

  public RankingController(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }

  @GetMapping("/ranking")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String ranking(@RequestParam(name = "body") long bodyId,
                        @RequestParam(name = "kind") @NotNull StatisticsKindDto kind,
                        Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ranking", statisticsService.getLatestRanking(bodyId, kind));
    return "ranking";
  }
}
