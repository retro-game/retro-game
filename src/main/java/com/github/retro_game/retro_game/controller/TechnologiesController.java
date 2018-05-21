package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.TechnologyService;
import com.github.retro_game.retro_game.service.dto.TechnologyKindDto;
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
public class TechnologiesController {
  private final TechnologyService technologyService;

  public TechnologiesController(TechnologyService technologyService) {
    this.technologyService = technologyService;
  }

  @GetMapping("/technologies")
  public String technologies(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("pair", technologyService.getTechnologiesAndQueuePair(bodyId));
    return "technologies";
  }

  @PostMapping("/technologies/research")
  public String research(@RequestParam(name = "body") long bodyId,
                         @RequestParam @Valid @NotNull TechnologyKindDto kind) {
    technologyService.research(bodyId, kind);
    return "redirect:/technologies?body=" + bodyId;
  }

  @PostMapping("/technologies/move-down")
  public String moveDown(@RequestParam(name = "body") long bodyId,
                         @RequestParam(name = "sequence-number") int sequenceNumber) {
    technologyService.moveDown(bodyId, sequenceNumber);
    return "redirect:/technologies?body=" + bodyId;
  }

  @PostMapping("/technologies/move-up")
  public String moveUp(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "sequence-number") int sequenceNumber) {
    technologyService.moveUp(bodyId, sequenceNumber);
    return "redirect:/technologies?body=" + bodyId;
  }

  @PostMapping("/technologies/cancel")
  public String cancel(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "sequence-number") int sequenceNumber) {
    technologyService.cancel(bodyId, sequenceNumber);
    return "redirect:/technologies?body=" + bodyId;
  }
}
