package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.*;
import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.service.BodyService;
import com.github.retro_game.retro_game.service.FlightService;
import com.github.retro_game.retro_game.service.PartyService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.service.exception.*;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Controller
public class FlightsController {
  private final BodyService bodyService;
  private final FlightService flightService;
  private final PartyService partyService;
  private final UserService userService;

  public FlightsController(BodyService bodyService, FlightService flightService, PartyService partyService,
                           UserService userService) {
    this.bodyService = bodyService;
    this.flightService = flightService;
    this.partyService = partyService;
    this.userService = userService;
  }

  private String perform(long bodyId, String page, Supplier<Integer> action) {
    FlightErrorDto error = null;
    try {
      action.get();
    } catch (BodyDoesNotExistException e) {
      error = FlightErrorDto.BODY_DOES_NOT_EXIST;
    } catch (DebrisFieldDoesNotExistException e) {
      error = FlightErrorDto.DEBRIS_FIELD_DOES_NOT_EXIST;
    } catch (FlightDoesNotExistException e) {
      error = FlightErrorDto.FLIGHT_DOES_NOT_EXIST;
    } catch (HoldTimeNotSpecifiedException e) {
      error = FlightErrorDto.HOLD_TIME_NOT_SPECIFIED;
    } catch (NoobProtectionException e) {
      error = FlightErrorDto.NOOB_PROTECTION;
    } catch (NotEnoughCapacityException e) {
      error = FlightErrorDto.NOT_ENOUGH_CAPACITY;
    } catch (NotEnoughDeuteriumException e) {
      error = FlightErrorDto.NOT_ENOUGH_DEUTERIUM;
    } catch (NotEnoughUnitsException e) {
      error = FlightErrorDto.NOT_ENOUGH_UNITS;
    } catch (NoColonyShipSelectedException e) {
      error = FlightErrorDto.NO_COLONY_SHIP_SELECTED;
    } catch (NoEspionageProbeSelectedException e) {
      error = FlightErrorDto.NO_ESPIONAGE_PROBE_SELECTED;
    } catch (NoMoreFreeSlotsException e) {
      error = FlightErrorDto.NO_MORE_FREE_SLOTS;
    } catch (NoRecyclerSelectedException e) {
      error = FlightErrorDto.NO_RECYCLER_SELECTED;
    } catch (NoUnitSelectedException e) {
      error = FlightErrorDto.NO_UNIT_SELECTED;
    } catch (PartyDoesNotExistException e) {
      error = FlightErrorDto.PARTY_DOES_NOT_EXIST;
    } catch (TargetOnVacationException e) {
      error = FlightErrorDto.TARGET_ON_VACATION;
    } catch (TargetOutOfRangeException e) {
      error = FlightErrorDto.TARGET_OUT_OF_RANGE;
    } catch (TooLateException e) {
      error = FlightErrorDto.TOO_LATE;
    } catch (TooManyPartyFlightsException e) {
      error = FlightErrorDto.TOO_MANY_PARTY_FLIGHTS;
    } catch (UnauthorizedFlightAccessException e) {
      error = FlightErrorDto.UNAUTHORIZED_FLIGHT_ACCESS;
    } catch (UnauthorizedPartyAccessException e) {
      error = FlightErrorDto.UNAUTHORIZED_PARTY_ACCESS;
    } catch (UnrecallableFlightException e) {
      error = FlightErrorDto.UNRECALLABLE_FLIGHT;
    } catch (WrongMissionException e) {
      error = FlightErrorDto.WRONG_MISSION;
    } catch (WrongTargetException e) {
      error = FlightErrorDto.WRONG_TARGET;
    } catch (WrongTargetKindException e) {
      error = FlightErrorDto.WRONG_TARGET_KIND;
    } catch (WrongTargetUserException e) {
      error = FlightErrorDto.WRONG_TARGET_USER;
    } catch (ConcurrencyFailureException e) {
      error = FlightErrorDto.CONCURRENCY;
    }
    if (error != null) {
      return "redirect:" + page + "?body=" + bodyId + "&error=" + error;
    }
    return "redirect:/flights?body=" + bodyId;
  }

  @GetMapping("/flights")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String flights(@RequestParam(name = "body") long bodyId,
                        @RequestParam(required = false) FlightErrorDto error,
                        Device device, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    var flights = flightService.getFlights(bodyId);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("error", error);
    model.addAttribute("ctx", ctx);
    model.addAttribute("flights", flights);
    model.addAttribute("occupiedSlots", flights.size());
    model.addAttribute("maxSlots", flightService.getMaxFlightSlots(bodyId));
    return Utils.getAppropriateView(device, "flights");
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
                     @RequestParam(required = false) Long metal,
                     @RequestParam(required = false) Long crystal,
                     @RequestParam(required = false) Long deuterium,
                     @RequestParam(required = false) Map<String, String> params,
                     @RequestParam(required = false) FlightErrorDto error,
                     Device device, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
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
    model.addAttribute("metal", metal);
    model.addAttribute("crystal", crystal);
    model.addAttribute("deuterium", deuterium);
    model.addAttribute("params", params);
    model.addAttribute("error", error);
    model.addAttribute("ctx", ctx);
    model.addAttribute("occupiedSlots", flightService.getOccupiedFlightSlots(bodyId));
    model.addAttribute("maxSlots", flightService.getMaxFlightSlots(bodyId));
    model.addAttribute("units", flightService.getFlyableUnits(bodyId));
    model.addAttribute("bodies", bodies);
    model.addAttribute("parties", partyService.getPartiesTargets(bodyId));
    return Utils.getAppropriateView(device, "flights-send");
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
    return perform(form.getBody(), "/flights/send", () -> {
      flightService.send(params);
      return 0;
    });
  }

  @GetMapping("/flights/send-missiles")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String sendMissiles(@RequestParam(name = "body") long bodyId,
                             @RequestParam(required = false) Integer galaxy,
                             @RequestParam(required = false) Integer system,
                             @RequestParam(required = false) Integer position,
                             @RequestParam(required = false) CoordinatesKindDto kind,
                             @RequestParam(required = false) FlightErrorDto error,
                             @RequestParam(required = false) Integer num,
                             Device device, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    var maxMissiles = ctx.curBody().units().get(UnitKindDto.INTERPLANETARY_MISSILE);
    var mainTargetKinds = UnitItem.getDefense().keySet().stream()
        .filter(k -> k != UnitKind.ANTI_BALLISTIC_MISSILE && k != UnitKind.INTERPLANETARY_MISSILE)
        .toList();
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("galaxy", galaxy);
    model.addAttribute("system", system);
    model.addAttribute("position", position);
    model.addAttribute("kind", kind);
    model.addAttribute("numMissiles", num);
    model.addAttribute("maxMissiles", maxMissiles);
    model.addAttribute("mainTargetKinds", mainTargetKinds);
    model.addAttribute("error", error);
    return Utils.getAppropriateView(device, "flights-send-missiles");
  }

  @PostMapping("/flights/send-missiles")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String doSendMissiles(@Valid SendMissilesForm form) {
    var target = new CoordinatesDto(form.getGalaxy(), form.getSystem(), form.getPosition(), form.getKind());
    return perform(form.getBody(), "/flights/send-missiles", () -> {
      flightService.sendMissiles(form.getBody(), target, form.getNumMissiles(), form.getMainTarget());
      return 0;
    });
  }

  @PostMapping("/flights/recall")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String recall(@Valid RecallFlightForm form) {
    return perform(form.getBody(), "/flights", () -> {
      flightService.recall(form.getBody(), form.getFlight());
      return 0;
    });
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
    } catch (NoUnitSelectedException | NotEnoughUnitsException e) {
      response.setError("NOT_ENOUGH_UNITS");
    } catch (ConcurrencyFailureException e) {
      response.setError("CONCURRENCY");
    }
    return response;
  }
}
