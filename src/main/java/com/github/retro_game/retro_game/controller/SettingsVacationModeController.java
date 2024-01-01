package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SettingsVacationModeController {
  private final UserService userService;

  public SettingsVacationModeController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/settings/vacation-mode")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String vacationMode(@RequestParam(name = "body") long bodyId, Device device, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("canEnable", userService.canEnableVacationMode());
    model.addAttribute("canDisable", userService.canDisableVacationMode());
    return Utils.getAppropriateView(device, "settings-vacation-mode");
  }

  @PostMapping("/settings/vacation-mode/enable")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String enableVacationMode(@RequestParam(name = "body") long bodyId) {
    userService.enableVacationMode();
    return "redirect:/settings/vacation-mode?body=" + bodyId;
  }

  @PostMapping("/settings/vacation-mode/disable")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String disableVacationMode(@RequestParam(name = "body") long bodyId) {
    userService.disableVacationMode();
    return "redirect:/settings/vacation-mode?body=" + bodyId;
  }
}
