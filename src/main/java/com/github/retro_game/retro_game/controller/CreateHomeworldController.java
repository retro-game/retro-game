package com.github.retro_game.retro_game.controller;

import com.github.retro_game.galaxy.GalaxyService;
import com.github.retro_game.retro_game.controller.form.CreateHomeworldForm;
import com.github.retro_game.retro_game.service.BodyService;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
@Validated
public class CreateHomeworldController {
  private final GalaxyService galaxyService;
  private final BodyService bodyService;

  public CreateHomeworldController(GalaxyService galaxyService, BodyService bodyService) {
    this.galaxyService = galaxyService;
    this.bodyService = bodyService;
  }

  @GetMapping("/create-homeworld")
  public String createHomeworld(@RequestParam @Range(min = 1, max = 5) int galaxy,
                                @RequestParam @Range(min = 1, max = 500) int system,
                                Model model) {
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("slots", galaxyService.getSlots(galaxy, system));
    return "create-homeworld";
  }

  @PostMapping("/create-homeworld")
  public String doCreateHomeworld(@Valid CreateHomeworldForm form) {
    long bodyId = bodyService.createHomeworld(form.getGalaxy(), form.getSystem(), form.getPosition());
    return "redirect:/overview?body=" + bodyId;
  }
}
