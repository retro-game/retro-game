package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.dto.CoordinatesKindDto;
import com.github.retro_game.retro_game.service.BodyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmpireController {
  private final BodyService bodyService;

  public EmpireController(BodyService bodyService) {
    this.bodyService = bodyService;
  }

  @GetMapping("/empire")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  public String empire(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "galaxy", required = false) Integer galaxy,
                       @RequestParam(name = "system", required = false) Integer system,
                       @RequestParam(name = "position", required = false) Integer position,
                       @RequestParam(name = "kind", required = false) CoordinatesKindDto kind,
                       Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("position", position);
    model.addAttribute("kind", kind);
    model.addAttribute("empire", bodyService.getEmpire(bodyId, galaxy, system, position, kind));
    return "empire";
  }
}
