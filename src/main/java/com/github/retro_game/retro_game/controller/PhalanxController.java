package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.PhalanxService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Date;

@Controller
public class PhalanxController {
  private final PhalanxService phalanxService;

  public PhalanxController(PhalanxService phalanxService) {
    this.phalanxService = phalanxService;
  }

  @GetMapping("/phalanx")
  public String phalanx(@RequestParam(name = "body") long bodyId,
                        @RequestParam int galaxy,
                        @RequestParam int system,
                        @RequestParam int position,
                        Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("position", position);
    model.addAttribute("time", Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())));
    model.addAttribute("events", phalanxService.scan(bodyId, galaxy, system, position));
    return "phalanx";
  }
}
