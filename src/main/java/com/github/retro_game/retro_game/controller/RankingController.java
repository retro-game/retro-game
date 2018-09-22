package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.RankingService;
import com.github.retro_game.retro_game.service.dto.RankingKindDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RankingController {
  private final RankingService rankingService;

  public RankingController(RankingService rankingService) {
    this.rankingService = rankingService;
  }

  @GetMapping("/ranking")
  public String ranking(@RequestParam(name = "body") long bodyId,
                        @RequestParam(name = "kind", required = false) RankingKindDto kind,
                        Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ranking", rankingService.getLatest(bodyId, kind));
    return "ranking";
  }
}
