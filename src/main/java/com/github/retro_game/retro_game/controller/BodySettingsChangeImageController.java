package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.BodyService;
import com.github.retro_game.retro_game.service.UserService;
import org.hibernate.validator.constraints.Range;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Validated
public class BodySettingsChangeImageController {
  private final BodyService bodyService;
  private final UserService userService;

  public BodySettingsChangeImageController(BodyService bodyService, UserService userService) {
    this.bodyService = bodyService;
    this.userService = userService;
  }

  @GetMapping("/body-settings/change-image")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String changeImage(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    return "body-settings-change-image";
  }

  @PostMapping("/body-settings/change-image")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String doChangeImage(@RequestParam(name = "body") long bodyId,
                              @RequestParam @Range(min = 1, max = 10) int image) {
    bodyService.setImage(bodyId, image);
    return "redirect:/overview?body=" + bodyId;
  }
}
