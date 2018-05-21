package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.DetailsService;
import com.github.retro_game.retro_game.service.dto.BuildingKindDto;
import com.github.retro_game.retro_game.service.dto.TechnologyKindDto;
import com.github.retro_game.retro_game.service.dto.UnitKindDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DetailsController {
  private final DetailsService detailsService;

  public DetailsController(DetailsService detailsService) {
    this.detailsService = detailsService;
  }

  @GetMapping("/details/building")
  public String buildingDetails(@RequestParam(name = "body") long bodyId, @RequestParam BuildingKindDto kind,
                                Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("kind", kind);
    model.addAttribute("details", detailsService.getBuildingDetails(bodyId, kind));
    return "details-building";
  }

  @GetMapping("/details/technology")
  public String technologyDetails(@RequestParam(name = "body") long bodyId, @RequestParam TechnologyKindDto kind,
                                  Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("kind", kind);
    return "details-technology";
  }

  @GetMapping("/details/unit")
  public String unitDetails(@RequestParam(name = "body") long bodyId, @RequestParam UnitKindDto kind, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("kind", kind);
    model.addAttribute("details", detailsService.getUnitDetails(bodyId, kind));
    return "details-unit";
  }
}
