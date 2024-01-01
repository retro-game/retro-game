package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.BuildUnitsForm;
import com.github.retro_game.retro_game.dto.ShipyardQueueErrorDto;
import com.github.retro_game.retro_game.dto.UnitTypeDto;
import com.github.retro_game.retro_game.service.ShipyardService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.service.exception.*;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.mobile.device.Device;
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
  private final UserService userService;

  public ShipyardController(ShipyardService shipyardService, UserService userService) {
    this.shipyardService = shipyardService;
    this.userService = userService;
  }

  @GetMapping("/shipyard")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String shipyard(@RequestParam(name = "body") long bodyId,
                         @RequestParam(required = false) UnitTypeDto type,
                         @RequestParam(name = "error", required = false) ShipyardQueueErrorDto error,
                         Device device, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);

    var p = ctx.curBody().production();
    var neededSatellites = 0;
    if (p.availableEnergy() < 0 && p.singleSolarSatelliteEnergyProduction() > 0)
      neededSatellites = (int) Math.ceil(-(double) p.availableEnergy() / p.singleSolarSatelliteEnergyProduction());

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("type", type);
    model.addAttribute("error", error);
    model.addAttribute("neededSatellites", neededSatellites);
    model.addAttribute("pair", shipyardService.getUnitsAndQueuePair(bodyId, type));
    return Utils.getAppropriateView(device, "shipyard");
  }

  @PostMapping("/shipyard/build")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String build(@Valid BuildUnitsForm form) {
    ShipyardQueueErrorDto error = null;
    try {
      shipyardService.build(form.getBody(), form.getKind(), form.getCount());
    } catch (RequirementsNotMetException e) {
      error = ShipyardQueueErrorDto.REQUIREMENTS_NOT_MET;
    } catch (NotEnoughResourcesException e) {
      error = ShipyardQueueErrorDto.NOT_ENOUGH_RESOURCES;
    } catch (TooManyShieldDomesException e) {
      error = ShipyardQueueErrorDto.TOO_MANY_SHIELD_DOMES;
    } catch (ShieldDomeAlreadyBuiltException e) {
      error = ShipyardQueueErrorDto.SHIELD_DOME_ALREADY_BUILT;
    } catch (ShieldDomeAlreadyInQueueException e) {
      error = ShipyardQueueErrorDto.SHIELD_DOME_ALREADY_IN_QUEUE;
    } catch (NotEnoughCapacityException e) {
      error = ShipyardQueueErrorDto.NOT_ENOUGH_CAPACITY;
    } catch (ConcurrencyFailureException e) {
      error = ShipyardQueueErrorDto.CONCURRENCY;
    }
    return "redirect:/shipyard?body=" + form.getBody() +
        (form.getType() != null ? "&type=" + form.getType() : "") +
        (error != null ? "&error=" + error : "");
  }
}
