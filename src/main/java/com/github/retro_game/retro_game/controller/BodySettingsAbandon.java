package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.BodyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Controller
@Validated
public class BodySettingsAbandon {
  private final BodyService bodyService;

  public BodySettingsAbandon(BodyService bodyService) {
    this.bodyService = bodyService;
  }

  @GetMapping("/body-settings/abandon")
  public String abandon(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    return "body-settings-abandon";
  }

  @PostMapping("/body-settings/abandon")
  public String doAbandon(@RequestParam(name = "body") long bodyId,
                          @Valid @NotNull String password) {
    long id = bodyService.abandonPlanet(bodyId, password);
    return "redirect:/overview?body=" + id;
  }
}
