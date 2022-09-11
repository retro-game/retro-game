package com.github.retro_game.retro_game.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  private final boolean enablePasswordRecovery;

  public HomeController(@Value("${retro-game.enable-password-recovery}") boolean enablePasswordRecovery) {
    this.enablePasswordRecovery = enablePasswordRecovery;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("enablePasswordRecovery", enablePasswordRecovery);
    return "home";
  }
}
