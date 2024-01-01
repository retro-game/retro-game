package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.BuildingKindDto;
import com.github.retro_game.retro_game.dto.TechnologyKindDto;
import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.service.BodyService;
import com.github.retro_game.retro_game.service.DetailsService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DetailsController {
  private final BodyService bodyService;
  private final DetailsService detailsService;
  private final UserService userService;

  public DetailsController(BodyService bodyService, DetailsService detailsService, UserService userService) {
    this.bodyService = bodyService;
    this.detailsService = detailsService;
    this.userService = userService;
  }

  @GetMapping("/details/building")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String buildingDetails(@RequestParam(name = "body") long bodyId, @RequestParam BuildingKindDto kind,
                                Device device, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    var energyTechnologyLevel = ctx.technologies().getOrDefault(TechnologyKindDto.ENERGY_TECHNOLOGY, 0);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("kind", kind);
    model.addAttribute("ctx", ctx);
    model.addAttribute("energyTechnologyLevel", energyTechnologyLevel);
    model.addAttribute("details", detailsService.getBuildingDetails(bodyId, kind));
    model.addAttribute("temperature", bodyService.getTemperature(bodyId));
    return Utils.getAppropriateView(device, "details-building");
  }

  @GetMapping("/details/technology")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String technologyDetails(@RequestParam(name = "body") long bodyId, @RequestParam TechnologyKindDto kind,
                                  Device device, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("kind", kind);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("details", detailsService.getTechnologyDetails(bodyId, kind));
    return Utils.getAppropriateView(device, "details-technology");
  }

  @GetMapping("/details/unit")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String unitDetails(@RequestParam(name = "body") long bodyId, @RequestParam UnitKindDto kind, Device device, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("kind", kind);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("details", detailsService.getUnitDetails(bodyId, kind));
    return Utils.getAppropriateView(device, "details-unit");
  }
}
