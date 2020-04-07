package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.RapidFireTableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RapidFireTableController {
  private final RapidFireTableService rapidFireTableService;

  public RapidFireTableController(RapidFireTableService rapidFireTableService) {
    this.rapidFireTableService = rapidFireTableService;
  }

  @GetMapping("/rapid-fire-table")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String rapidFireTable(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("table", rapidFireTableService.getRapidFireTable(bodyId));
    return "rapid-fire-table";
  }
}
