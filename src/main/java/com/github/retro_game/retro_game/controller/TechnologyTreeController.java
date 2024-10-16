package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.TechnologyTreeService;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TechnologyTreeController {
  private final TechnologyTreeService technologyTreeService;
  private final UserService userService;

  public TechnologyTreeController(TechnologyTreeService technologyTreeService, UserService userService) {
    this.technologyTreeService = technologyTreeService;
    this.userService = userService;
  }

  @GetMapping("/technology-tree")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String technologyTree(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("tree", technologyTreeService.getTechnologyTree(bodyId));
    return "technology-tree";
  }
}
