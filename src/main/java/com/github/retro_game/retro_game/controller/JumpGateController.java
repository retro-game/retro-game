package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.JumpForm;
import com.github.retro_game.retro_game.service.JumpGateService;
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

  public JumpGateController(JumpGateService jumpGateService) {
    this.jumpGateService = jumpGateService;
  }

  @GetMapping("/jump-gate")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String jumpGate(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("info", jumpGateService.getInfo(bodyId));
    return "jump-gate";
  }

  @PostMapping("/jump-gate/jump")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS') && hasPermission(#form.target, 'ACCESS')")
  @Activity(bodies = {"#form.body", "#form.target"})
  public String jump(@Valid JumpForm form) {
    jumpGateService.jump(form.getBody(), form.getTarget(), form.getUnits());
    return "redirect:/jump-gate?body=" + form.getBody();
  }
}
