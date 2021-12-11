package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.BodyService;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;

@Controller
@Validated
public class BodySettingsAbandon {
  private final BodyService bodyService;
  private final UserService userService;

  public BodySettingsAbandon(BodyService bodyService, UserService userService) {
    this.bodyService = bodyService;
    this.userService = userService;
  }

  @GetMapping("/body-settings/abandon")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String abandon(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    return "body-settings-abandon";
  }

  @PostMapping("/body-settings/abandon")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String doAbandon(@RequestParam(name = "body") long bodyId,
                          @NotNull String password) {
    long id = bodyService.abandonPlanet(bodyId, password);
    return "redirect:/overview?body=" + id;
  }
}
