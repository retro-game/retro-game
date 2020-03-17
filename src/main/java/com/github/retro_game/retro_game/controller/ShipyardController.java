package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.BuildUnitsForm;
import com.github.retro_game.retro_game.dto.UnitTypeDto;
import com.github.retro_game.retro_game.service.ShipyardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class ShipyardController {
  private final ShipyardService shipyardService;

  public ShipyardController(ShipyardService shipyardService) {
    this.shipyardService = shipyardService;
  }

  @GetMapping("/shipyard")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  public String shipyard(@RequestParam(name = "body") long bodyId, @RequestParam(required = false) UnitTypeDto type,
                         Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("type", type);
    model.addAttribute("pair", shipyardService.getUnitsAndQueuePair(bodyId, type));
    return "shipyard";
  }

  @PostMapping("/shipyard/build")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  public String build(@Valid BuildUnitsForm form) {
    shipyardService.build(form.getBody(), form.getKind(), form.getCount());
    return "redirect:/shipyard?body=" + form.getBody() + (form.getType() != null ? "&type=" + form.getType() : "");
  }
}
