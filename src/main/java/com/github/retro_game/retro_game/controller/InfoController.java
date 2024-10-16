package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InfoController {
  private final UserService userService;

  public InfoController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/info")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String info(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    return "info";
  }
}
