package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BodySettingsController {
  private final UserService userService;

  public BodySettingsController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/body-settings")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String bodySettings(@RequestParam(name = "body") long bodyId, Device device, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    return Utils.getAppropriateView(device, "body-settings");
  }
}
