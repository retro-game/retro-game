package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.PrangerEntryDto;
import com.github.retro_game.retro_game.service.PrangerService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PrangerController {
  private final PrangerService prangerService;
  private final UserService userService;

  public PrangerController(PrangerService prangerService, UserService userService) {
    this.prangerService = prangerService;
    this.userService = userService;
  }

  @GetMapping("/pranger")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String messages(@RequestParam(name = "body") long bodyId, Device device, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    List<PrangerEntryDto> pranger = prangerService.get(bodyId);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("pranger", pranger);
    return Utils.getAppropriateView(device, "pranger");
  }
}
