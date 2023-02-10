package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.BuildingKindDto;
import com.github.retro_game.retro_game.dto.BuildingQueueErrorDto;
import com.github.retro_game.retro_game.service.BuildingsService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.service.exception.*;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

@Controller
@Validated
public class BuildingsController {
  private final BuildingsService buildingsService;
  private final UserService userService;

  public BuildingsController(BuildingsService buildingsService, UserService userService) {
    this.buildingsService = buildingsService;
    this.userService = userService;
  }

  @GetMapping("/buildings")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String buildings(@RequestParam(name = "body") long bodyId,
                          @RequestParam(name = "error", required = false) BuildingQueueErrorDto error,
                          Device device, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("error", error);
    model.addAttribute("pair", buildingsService.getBuildingsAndQueuePair(bodyId));
    return Utils.getAppropriateView(device, "buildings");
  }

  private String perform(long bodyId, Supplier<Integer> action) {
    BuildingQueueErrorDto error = null;
    try {
      action.get();
    } catch (BuildingAlreadyDestroyedException e) {
      error = BuildingQueueErrorDto.BUILDING_ALREADY_DESTROYED;
    } catch (CannotCancelException e) {
      error = BuildingQueueErrorDto.CANNOT_CANCEL;
    } catch (CannotMoveException e) {
      error = BuildingQueueErrorDto.CANNOT_MOVE;
    } catch (MissingEventException e) {
      error = BuildingQueueErrorDto.MISSING_EVENT;
    } catch (NotEnoughEnergyException e) {
      error = BuildingQueueErrorDto.NOT_ENOUGH_ENERGY;
    } catch (NotEnoughResourcesException e) {
      error = BuildingQueueErrorDto.NOT_ENOUGH_RESOURCES;
    } catch (NoMoreFreeFieldsException e) {
      error = BuildingQueueErrorDto.NO_MORE_FREE_FIELDS;
    } catch (NoSuchQueueEntryException e) {
      error = BuildingQueueErrorDto.NO_SUCH_QUEUE_ENTRY;
    } catch (QueueFullException e) {
      error = BuildingQueueErrorDto.QUEUE_FULL;
    } catch (RequirementsNotMetException e) {
      error = BuildingQueueErrorDto.REQUIREMENTS_NOT_MET;
    } catch (WrongBuildingKindException e) {
      error = BuildingQueueErrorDto.WRONG_BUILDING_KIND;
    } catch (ConcurrencyFailureException e) {
      error = BuildingQueueErrorDto.CONCURRENCY;
    }
    return "redirect:/buildings?body=" + bodyId + (error != null ? "&error=" + error : "");
  }

  @PostMapping("/buildings/construct")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String construct(@RequestParam(name = "body") long bodyId,
                          @RequestParam @NotNull BuildingKindDto kind) {
    return perform(bodyId, () -> {
      buildingsService.construct(bodyId, kind);
      return 0;
    });
  }

  @PostMapping("/buildings/destroy")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String destroy(@RequestParam(name = "body") long bodyId,
                        @RequestParam @NotNull BuildingKindDto kind) {
    return perform(bodyId, () -> {
      buildingsService.destroy(bodyId, kind);
      return 0;
    });
  }

  @PostMapping("/buildings/move-down")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String moveDown(@RequestParam(name = "body") long bodyId,
                         @RequestParam(name = "sequence-number") int sequenceNumber) {
    return perform(bodyId, () -> {
      buildingsService.moveDown(bodyId, sequenceNumber);
      return 0;
    });
  }

  @PostMapping("/buildings/move-up")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String moveUp(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "sequence-number") int sequenceNumber) {
    return perform(bodyId, () -> {
      buildingsService.moveUp(bodyId, sequenceNumber);
      return 0;
    });
  }

  @PostMapping("/buildings/cancel")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String cancel(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "sequence-number") int sequenceNumber) {
    return perform(bodyId, () -> {
      buildingsService.cancel(bodyId, sequenceNumber);
      return 0;
    });
  }
}
