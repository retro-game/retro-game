package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.PhalanxService;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Date;

@Controller
public class PhalanxController {
  private final PhalanxService phalanxService;
  private final UserService userService;

  public PhalanxController(PhalanxService phalanxService, UserService userService) {
    this.phalanxService = phalanxService;
    this.userService = userService;
  }

  @GetMapping("/phalanx")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String phalanx(@RequestParam(name = "body") long bodyId,
                        @RequestParam int galaxy,
                        @RequestParam int system,
                        @RequestParam int position,
                        Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("position", position);
    model.addAttribute("time", Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())));
    model.addAttribute("events", phalanxService.scan(bodyId, galaxy, system, position));
    return "phalanx";
  }
}
