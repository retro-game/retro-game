package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.*;
import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.service.BodyService;
import com.github.retro_game.retro_game.service.FlightService;
import com.github.retro_game.retro_game.service.PartyService;
import com.github.retro_game.retro_game.service.exception.NoMoreFreeSlotsException;
import com.github.retro_game.retro_game.service.exception.NotEnoughCapacityException;
import com.github.retro_game.retro_game.service.exception.NotEnoughDeuteriumException;
import com.github.retro_game.retro_game.service.exception.NotEnoughUnitsException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class FlightsController {
  private final BodyService bodyService;
  private final FlightService flightService;
  private final PartyService partyService;

  public FlightsController(BodyService bodyService, FlightService flightService, PartyService partyService) {
    this.bodyService = bodyService;
    this.flightService = flightService;
    this.partyService = partyService;
  }

  @GetMapping("/flights")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String flights(@RequestParam(name = "body") long bodyId, Model model) {
    List<FlightDto> flights = flightService.getFlights(bodyId);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("flights", flights);
    model.addAttribute("occupiedSlots", flights.size());
    model.addAttribute("maxSlots", flightService.getMaxFlightSlots(bodyId));
    return "flights";
  }

  @GetMapping("/flights/send")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String send(@RequestParam(name = "body") long bodyId,
                     @RequestParam(required = false) Integer galaxy,
                     @RequestParam(required = false) Integer system,
                     @RequestParam(required = false) Integer position,
                     @RequestParam(required = false) CoordinatesKindDto kind,
                     @RequestParam(required = false) MissionDto mission,
                     @RequestParam(required = false) Map<String, String> params,
                     Model model) {
    List<BodyInfoDto> bodies = bodyService.getBodiesBasicInfo(bodyId);

    Optional<BodyInfoDto> curBodyOptional = bodies.stream().filter(i -> i.getId() == bodyId).findFirst();
    assert curBodyOptional.isPresent();
    BodyInfoDto curBody = curBodyOptional.get();

    CoordinatesDto coords = curBody.getCoordinates();
    int g = galaxy != null ? galaxy : coords.getGalaxy();
    int s = system != null ? system : coords.getSystem();
    int p = position != null ? position : coords.getPosition();
    CoordinatesKindDto k = kind != null ? kind : coords.getKind();

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("galaxy", g);
    model.addAttribute("system", s);
    model.addAttribute("position", p);
    model.addAttribute("kind", k);
    model.addAttribute("mission", mission);
    model.addAttribute("params", params);
    model.addAttribute("occupiedSlots", flightService.getOccupiedFlightSlots(bodyId));
    model.addAttribute("maxSlots", flightService.getMaxFlightSlots(bodyId));
    model.addAttribute("units", flightService.getFlyableUnits(bodyId));
    model.addAttribute("bodies", bodies);
    model.addAttribute("parties", partyService.getPartiesTargets(bodyId));
    return "flights-send";
  }

  @PostMapping("/flights/send")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String doSend(@Valid SendFleetForm form) {
    CoordinatesDto c = new CoordinatesDto(form.getGalaxy(), form.getSystem(), form.getPosition(), form.getKind());
    ResourcesDto r = new ResourcesDto(
        form.getMetal() != null ? form.getMetal() : 0.0,
        form.getCrystal() != null ? form.getCrystal() : 0.0,
        form.getDeuterium() != null ? form.getDeuterium() : 0.0);
    SendFleetParamsDto params = new SendFleetParamsDto(form.getBody(), form.getUnits(), form.getMission(),
        form.getHoldTime(), c, form.getFactor(), r, form.getParty());
    flightService.send(params);
    return "redirect:/flights?body=" + form.getBody();
  }

  @PostMapping("/flights/send-probes")
  @ResponseBody
  @PreAuthorize("hasPermission(#request.body, 'ACCESS')")
  @Activity(bodies = "#request.body")
  public SendProbesResponse sendProbes(@RequestBody @Valid SendProbesRequest request) {
    SendProbesResponse response = new SendProbesResponse();
    try {
      CoordinatesDto c = new CoordinatesDto(request.getGalaxy(), request.getSystem(), request.getPosition(),
          request.getKind());
      flightService.sendProbes(request.getBody(), c, request.getCount());
      response.setSuccess(true);
    } catch (NoMoreFreeSlotsException e) {
      response.setError("NO_MORE_FREE_SLOTS");
    } catch (NotEnoughDeuteriumException e) {
      response.setError("NOT_ENOUGH_DEUTERIUM");
    } catch (NotEnoughCapacityException e) {
      response.setError("NOT_ENOUGH_CAPACITY");
    } catch (NotEnoughUnitsException e) {
      response.setError("NOT_ENOUGH_UNITS");
    }
    return response;
  }

  @GetMapping("/flights/send-missiles")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String sendMissiles(@RequestParam(name = "body") long bodyId,
                             @RequestParam(required = false) Integer galaxy,
                             @RequestParam(required = false) Integer system,
                             @RequestParam(required = false) Integer position,
                             @RequestParam(required = false) CoordinatesKindDto kind,
                             Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("position", position);
    model.addAttribute("kind", kind);
    return "flights-send-missiles";
  }

  @PostMapping("/flights/send-missiles")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String doSendMissiles(@Valid SendMissilesForm form) {
    CoordinatesDto target = new CoordinatesDto(form.getGalaxy(), form.getSystem(), form.getPosition(), form.getKind());
    flightService.sendMissiles(form.getBody(), target, form.getNumMissiles());
    return "redirect:/flights?body=" + form.getBody();
  }

  @PostMapping("/flights/recall")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String recall(@Valid RecallFlightForm form) {
    flightService.recall(form.getBody(), form.getFlight());
    return "redirect:/flights?body=" + form.getBody();
  }
}
