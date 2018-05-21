package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.InviteToPartyForm;
import com.github.retro_game.retro_game.service.PartyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class PartyController {
  private final PartyService partyService;

  public PartyController(PartyService partyService) {
    this.partyService = partyService;
  }

  @GetMapping("/party")
  public String party(@RequestParam(name = "body") long bodyId, @RequestParam(name = "party") long partyId,
                      Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("party", partyService.get(bodyId, partyId));
    return "party";
  }

  @PostMapping("/party/create")
  public String create(@RequestParam(name = "body") long bodyId, @RequestParam(name = "flight") long flightId) {
    long partyId = partyService.create(bodyId, flightId);
    return "redirect:/party?body=" + bodyId + "&party=" + partyId;
  }

  @PostMapping("/party/invite")
  public String invite(@Valid InviteToPartyForm form) {
    partyService.invite(form.getBody(), form.getParty(), form.getName());
    return "redirect:/party?body=" + form.getBody() + "&party=" + form.getParty();
  }
}
