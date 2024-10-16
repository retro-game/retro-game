package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.CoordinatesKindDto;
import com.github.retro_game.retro_game.service.BodyService;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmpireController {
  private final BodyService bodyService;
  private final UserService userService;

  public EmpireController(BodyService bodyService, UserService userService) {
    this.bodyService = bodyService;
    this.userService = userService;
  }

  @GetMapping("/empire")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String empire(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "galaxy", required = false) Integer galaxy,
                       @RequestParam(name = "system", required = false) Integer system,
                       @RequestParam(name = "position", required = false) Integer position,
                       @RequestParam(name = "kind", required = false) CoordinatesKindDto kind,
                       Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("position", position);
    model.addAttribute("kind", kind);
    model.addAttribute("ctx", ctx);
    model.addAttribute("empire", bodyService.getEmpire(bodyId, galaxy, system, position, kind));
    return "empire";
  }
}
