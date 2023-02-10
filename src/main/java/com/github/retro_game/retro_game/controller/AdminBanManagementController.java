package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Controller
@Validated
public class AdminBanManagementController {
  private final UserService userService;

  public AdminBanManagementController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/admin/ban-management")
  public String banManagement(Device device) {
    return Utils.getAppropriateView(device, "admin-ban-management");
  }

  @PostMapping("/admin/ban-management/ban")
  public String ban(@RequestParam @NotBlank String name,
                    @RequestParam @Min(1) int duration,
                    @RequestParam @NotBlank String reason) {
    userService.ban(name, duration, reason);
    return "redirect:/admin/ban-management?banned";
  }

  @PostMapping("/admin/ban-management/unban")
  public String unban(@RequestParam @NotBlank String name) {
    userService.unban(name);
    return "redirect:/admin/ban-management?unbanned";
  }
}
