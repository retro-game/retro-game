package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.BuildingsService;
import com.github.retro_game.retro_game.service.dto.BuildingKindDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Controller
@Validated
public class BuildingsController {
  private final BuildingsService buildingsService;

  public BuildingsController(BuildingsService buildingsService) {
    this.buildingsService = buildingsService;
  }

  @GetMapping("/buildings")
  public String buildings(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("pair", buildingsService.getBuildingsAndQueuePair(bodyId));
    return "buildings";
  }

  @PostMapping("/buildings/construct")
  public String construct(@RequestParam(name = "body") long bodyId,
                          @RequestParam @Valid @NotNull BuildingKindDto kind) {
    buildingsService.construct(bodyId, kind);
    return "redirect:/buildings?body=" + bodyId;
  }

  @PostMapping("/buildings/destroy")
  public String destroy(@RequestParam(name = "body") long bodyId,
                        @RequestParam @Valid @NotNull BuildingKindDto kind) {
    buildingsService.destroy(bodyId, kind);
    return "redirect:/buildings?body=" + bodyId;
  }

  @PostMapping("/buildings/move-down")
  public String moveDown(@RequestParam(name = "body") long bodyId,
                         @RequestParam(name = "sequence-number") int sequenceNumber) {
    buildingsService.moveDown(bodyId, sequenceNumber);
    return "redirect:/buildings?body=" + bodyId;
  }

  @PostMapping("/buildings/move-up")
  public String moveUp(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "sequence-number") int sequenceNumber) {
    buildingsService.moveUp(bodyId, sequenceNumber);
    return "redirect:/buildings?body=" + bodyId;
  }

  @PostMapping("/buildings/cancel")
  public String cancel(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "sequence-number") int sequenceNumber) {
    buildingsService.cancel(bodyId, sequenceNumber);
    return "redirect:/buildings?body=" + bodyId;
  }
}
