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
public class UniSettingsController {

  public UniSettingsController() {

  }

  @GetMapping("/uni-settings")
  public String info(Device device) {
    return Utils.getAppropriateView(device, "uni-settings");
  }
}
