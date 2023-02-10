package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.InviteToPartyForm;
import com.github.retro_game.retro_game.service.PartyService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class PartyController {
  private final PartyService partyService;
  private final UserService userService;

  public PartyController(PartyService partyService, UserService userService) {
    this.partyService = partyService;
    this.userService = userService;
  }

  @GetMapping("/party")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String party(@RequestParam(name = "body") long bodyId, @RequestParam(name = "party") long partyId,
                      Device device, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("party", partyService.get(bodyId, partyId));
    return Utils.getAppropriateView(device, "party");
  }

  @PostMapping("/party/create")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String create(@RequestParam(name = "body") long bodyId, @RequestParam(name = "flight") long flightId) {
    long partyId = partyService.create(bodyId, flightId);
    return "redirect:/party?body=" + bodyId + "&party=" + partyId;
  }

  @PostMapping("/party/invite")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String invite(@Valid InviteToPartyForm form) {
    partyService.invite(form.getBody(), form.getParty(), form.getName());
    return "redirect:/party?body=" + form.getBody() + "&party=" + form.getParty();
  }
}
