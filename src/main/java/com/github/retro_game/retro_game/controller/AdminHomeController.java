package com.github.retro_game.retro_game.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminHomeController {
  @GetMapping("/admin/")
  public String home() {
    return "admin-home";
  }
}
