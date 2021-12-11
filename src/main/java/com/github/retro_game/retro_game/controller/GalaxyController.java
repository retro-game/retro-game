package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.CoordinatesKindDto;
import com.github.retro_game.retro_game.service.GalaxyService;
import com.github.retro_game.retro_game.service.PhalanxService;
import com.github.retro_game.retro_game.service.UserService;
import org.hibernate.validator.constraints.Range;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Date;

@Controller
@Validated
public class GalaxyController {
  private final GalaxyService galaxyService;
  private final PhalanxService phalanxService;
  private final UserService userService;

  public GalaxyController(GalaxyService galaxyService, PhalanxService phalanxService, UserService userService) {
    this.galaxyService = galaxyService;
    this.phalanxService = phalanxService;
    this.userService = userService;
  }

  @GetMapping("/galaxy")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String galaxy(@RequestParam(name = "body") long bodyId,
                       @RequestParam @Range(min = 1, max = 5) int galaxy,
                       @RequestParam @Range(min = 1, max = 500) int system,
                       @RequestParam(required = false) @Range(min = 1, max = 15) Integer position,
                       @RequestParam(required = false) CoordinatesKindDto kind,
                       Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("position", position);
    model.addAttribute("kind", kind);
    var ctx = userService.getCurrentUserContext(bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("time", Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())));
    model.addAttribute("slots", galaxyService.getSlots(bodyId, galaxy, system));
    model.addAttribute("systemWithinRange", phalanxService.systemWithinRange(bodyId, galaxy, system));
    return "galaxy";
  }
}
