package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminHomeController {
  @GetMapping("/admin/")
  public String home(Device device) {
    return Utils.getAppropriateView(device, "admin-home");
  }
}
