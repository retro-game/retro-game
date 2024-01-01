package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.JumpForm;
import com.github.retro_game.retro_game.service.JumpGateService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class JumpGateController {
  private final JumpGateService jumpGateService;
  private final UserService userService;

  public JumpGateController(JumpGateService jumpGateService, UserService userService) {
    this.jumpGateService = jumpGateService;
    this.userService = userService;
  }

  @GetMapping("/jump-gate")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String jumpGate(@RequestParam(name = "body") long bodyId, Device device, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("info", jumpGateService.getInfo(bodyId));
    return Utils.getAppropriateView(device, "jump-gate");
  }

  @PostMapping("/jump-gate/jump")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS') && hasPermission(#form.target, 'ACCESS')")
  @Activity(bodies = {"#form.body", "#form.target"})
  public String jump(@Valid JumpForm form) {
    jumpGateService.jump(form.getBody(), form.getTarget(), form.getUnits());
    return "redirect:/jump-gate?body=" + form.getBody();
  }
}
