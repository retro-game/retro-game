package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.BuildingKindDto;
import com.github.retro_game.retro_game.dto.CoordinatesKindDto;
import com.github.retro_game.retro_game.dto.TechnologyKindDto;
import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.model.ItemUtils;
import com.github.retro_game.retro_game.service.GalaxyService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.service.impl.Converter;
import org.hibernate.validator.constraints.Range;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Date;

@Controller
@Validated
public class GalaxyController {
  private final GalaxyService galaxyService;
  private final UserService userService;

  public GalaxyController(GalaxyService galaxyService, UserService userService) {
    this.galaxyService = galaxyService;
    this.userService = userService;
  }

  @GetMapping("/galaxy")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String galaxy(@RequestParam(name = "body") long bodyId,
                       @RequestParam @Range(min = 1, max = 5) int galaxy,
                       @RequestParam @Range(min = 1, max = 500) int system,
                       @RequestParam(required = false) @Range(min = 1, max = 15) Integer position,
                       @RequestParam(required = false) CoordinatesKindDto kind,
                       Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    var startCoords = Converter.convert(ctx.curBody().coordinates());
    var numMissiles = ctx.curBody().units().get(UnitKindDto.INTERPLANETARY_MISSILE);

    var impulseDriveLevel = ctx.technologies().get(TechnologyKindDto.IMPULSE_DRIVE);
    var isWithinMissilesRange = ItemUtils.isWithinMissilesRange(startCoords, galaxy, system, impulseDriveLevel);

    var phalanxLevel = ctx.curBody().buildings().get(BuildingKindDto.SENSOR_PHALANX);
    var isWithinPhalanxRange = ItemUtils.isWithinPhalanxRange(startCoords, galaxy, system, phalanxLevel);

    var slots = galaxyService.getSlots(bodyId, galaxy, system);
    var now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("position", position);
    model.addAttribute("kind", kind);
    model.addAttribute("time", now);
    model.addAttribute("slots", slots);
    model.addAttribute("numMissiles", numMissiles);
    model.addAttribute("isWithinMissilesRange", isWithinMissilesRange);
    model.addAttribute("isWithinPhalanxRange", isWithinPhalanxRange);

    return "galaxy";
  }
}
