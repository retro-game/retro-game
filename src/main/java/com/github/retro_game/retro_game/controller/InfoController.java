package com.github.retro_game.retro_game.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InfoController {
  @GetMapping("/info")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  public String info(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    return "info";
  }
}
