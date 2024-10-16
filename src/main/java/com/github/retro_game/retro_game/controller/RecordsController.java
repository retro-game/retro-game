package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.RecordsService;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecordsController {
  private final RecordsService recordsService;
  private final UserService userService;

  public RecordsController(RecordsService recordsService, UserService userService) {
    this.recordsService = recordsService;
    this.userService = userService;
  }

  @GetMapping("/records")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String records(@RequestParam(name = "body") long bodyId, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    var records = recordsService.getRecords();
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("records", records);
    return "records";
  }

  @GetMapping("/records/share")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String share(@RequestParam(name = "body") long bodyId, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    return "records-share";
  }

  @PostMapping("/records/share")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String doShare(@RequestParam(name = "body") long bodyId,
                        @RequestParam(required = false) Boolean buildings,
                        @RequestParam(required = false) Boolean technologies,
                        @RequestParam(required = false) Boolean units,
                        @RequestParam(required = false) Boolean production,
                        @RequestParam(required = false) Boolean other) {
    if (buildings == null) buildings = false;
    if (technologies == null) technologies = false;
    if (units == null) units = false;
    if (production == null) production = false;
    if (other == null) other = false;
    recordsService.share(bodyId, buildings, technologies, units, production, other);
    return "redirect:/records?body=" + bodyId;
  }
}
