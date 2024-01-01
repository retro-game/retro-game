package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.BodyInfoDto;
import com.github.retro_game.retro_game.dto.TechnologyKindDto;
import com.github.retro_game.retro_game.dto.TechnologyQueueErrorDto;
import com.github.retro_game.retro_game.service.TechnologyService;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller
@Validated
public class TechnologiesController {
  private final TechnologyService technologyService;
  private final UserService userService;

  public TechnologiesController(TechnologyService technologyService, UserService userService) {
    this.technologyService = technologyService;
    this.userService = userService;
  }

  @GetMapping("/technologies")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String technologies(@RequestParam(name = "body") long bodyId,
                             @RequestParam(name = "error", required = false) TechnologyQueueErrorDto error,
                             Device device, Model model) {
    var ctx = userService.getCurrentUserContext(bodyId);
    var pair = technologyService.getTechnologiesAndQueuePair(bodyId);
    var bodiesInfo = ctx.bodies().stream().collect(Collectors.toMap(BodyInfoDto::getId, Function.identity()));
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", ctx);
    model.addAttribute("error", error);
    model.addAttribute("pair", pair);
    model.addAttribute("bodiesInfo", bodiesInfo);
    return Utils.getAppropriateView(device, "technologies");
  }

  private String perform(long bodyId, Supplier<Integer> action) {
    TechnologyQueueErrorDto error = null;
    try {
      action.get();
    } catch (CannotCancelException e) {
      error = TechnologyQueueErrorDto.CANNOT_CANCEL;
    } catch (CannotMoveException e) {
      error = TechnologyQueueErrorDto.CANNOT_MOVE;
    } catch (MissingEventException e) {
      error = TechnologyQueueErrorDto.MISSING_EVENT;
    } catch (NotEnoughEnergyException e) {
      error = TechnologyQueueErrorDto.NOT_ENOUGH_ENERGY;
    } catch (NotEnoughResourcesException e) {
      error = TechnologyQueueErrorDto.NOT_ENOUGH_RESOURCES;
    } catch (NoSuchQueueEntryException e) {
      error = TechnologyQueueErrorDto.NO_SUCH_QUEUE_ENTRY;
    } catch (QueueFullException e) {
      error = TechnologyQueueErrorDto.QUEUE_FULL;
    } catch (RequirementsNotMetException e) {
      error = TechnologyQueueErrorDto.REQUIREMENTS_NOT_MET;
    } catch (ConcurrencyFailureException e) {
      error = TechnologyQueueErrorDto.CONCURRENCY;
    }
    return "redirect:/technologies?body=" + bodyId + (error != null ? "&error=" + error : "");
  }

  @PostMapping("/technologies/research")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String research(@RequestParam(name = "body") long bodyId,
                         @RequestParam @NotNull TechnologyKindDto kind) {
    return perform(bodyId, () -> {
      technologyService.research(bodyId, kind);
      return 0;
    });
  }

  @PostMapping("/technologies/move-down")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String moveDown(@RequestParam(name = "body") long bodyId,
                         @RequestParam(name = "sequence-number") int sequenceNumber) {
    return perform(bodyId, () -> {
      technologyService.moveDown(bodyId, sequenceNumber);
      return 0;
    });
  }

  @PostMapping("/technologies/move-up")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String moveUp(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "sequence-number") int sequenceNumber) {
    return perform(bodyId, () -> {
      technologyService.moveUp(bodyId, sequenceNumber);
      return 0;
    });
  }

  @PostMapping("/technologies/cancel")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String cancel(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "sequence-number") int sequenceNumber) {
    return perform(bodyId, () -> {
      technologyService.cancel(bodyId, sequenceNumber);
      return 0;
    });
  }
}
