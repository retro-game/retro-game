package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.CreateHomeworldForm;
import com.github.retro_game.retro_game.service.BodyCreationService;
import com.github.retro_game.retro_game.service.GalaxyService;
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
  private final BodyCreationService bodyCreationService;
  private final GalaxyService galaxyService;

  public CreateHomeworldController(BodyCreationService bodyCreationService, GalaxyService galaxyService) {
    this.bodyCreationService = bodyCreationService;
    this.galaxyService = galaxyService;
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
    var body = bodyCreationService.createHomeworld(form.getGalaxy(), form.getSystem(), form.getPosition());
    return "redirect:/overview?body=" + body.getId();
  }
}
