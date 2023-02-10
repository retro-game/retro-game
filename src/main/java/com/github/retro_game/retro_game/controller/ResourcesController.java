package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.SetProductionFactorsForm;
import com.github.retro_game.retro_game.dto.ProductionFactorsDto;
import com.github.retro_game.retro_game.dto.ProductionItemsDto;
import com.github.retro_game.retro_game.service.BodyService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Date;

import static com.github.retro_game.retro_game.dto.TechnologyKindDto.PLASMA_TECHNOLOGY;

@Controller
public class ResourcesController {
  private final BodyService bodyService;
  private final UserService userService;

  private final boolean plasmaTechnologyAffectsProduction;

  public ResourcesController(
          BodyService bodyService,
          UserService userService,
          @Value("${retro-game.plasma-technology-affects-production}") boolean plasmaTechnologyAffectsProduction
  ) {
    this.bodyService = bodyService;
    this.userService = userService;
    this.plasmaTechnologyAffectsProduction = plasmaTechnologyAffectsProduction;
  }

  @GetMapping("/resources")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String resources(@RequestParam(name = "body") long bodyId, Device device, Model model) {
    model.addAttribute("bodyId", bodyId);

    var ctx = userService.getCurrentUserContext(bodyId);
    model.addAttribute("ctx", ctx);

    var production = ctx.curBody().production();
    model.addAttribute("production", production);

    ProductionItemsDto items = bodyService.getProductionItems(bodyId);
    model.addAttribute("items", items);

    ProductionFactorsDto factors = bodyService.getProductionFactors(bodyId);
    model.addAttribute("factors", factors);

    // Economy.

    double metal = production.metalProduction();
    double crystal = production.crystalProduction();
    double deuterium = production.deuteriumProduction();
    double sum = metal + crystal + deuterium;

    model.addAttribute("metalHourly", metal);
    model.addAttribute("crystalHourly", crystal);
    model.addAttribute("deuteriumHourly", deuterium);
    model.addAttribute("sumHourly", sum);

    model.addAttribute("metalDaily", 24 * metal);
    model.addAttribute("crystalDaily", 24 * crystal);
    model.addAttribute("deuteriumDaily", 24 * deuterium);
    model.addAttribute("sumDaily", 24 * sum);

    model.addAttribute("metalWeekly", 24 * 7 * metal);
    model.addAttribute("crystalWeekly", 24 * 7 * crystal);
    model.addAttribute("deuteriumWeekly", 24 * 7 * deuterium);
    model.addAttribute("sumWeekly", 24 * 7 * sum);

    model.addAttribute("metal30Days", 24 * 30 * metal);
    model.addAttribute("crystal30Days", 24 * 30 * crystal);
    model.addAttribute("deuterium30Days", 24 * 30 * deuterium);
    model.addAttribute("sum30Days", 24 * 30 * sum);

    model.addAttribute("plasmaTechnologyAffectsProduction", plasmaTechnologyAffectsProduction);
    model.addAttribute("plasmaTechnologyLevel", ctx.technologies().get(PLASMA_TECHNOLOGY));

    // Capacity.

    var resources = ctx.curBody().resources();
    var capacity = ctx.curBody().capacity();
    model.addAttribute("capacity", capacity);

    long now = Instant.now().getEpochSecond();

    double metalFullSeconds = (capacity.getMetal() - resources.getMetal()) * 3600.0 / metal;
    if (Double.isFinite(metalFullSeconds) && metalFullSeconds > 0.0) {
      model.addAttribute("metalFullAt", Date.from(Instant.ofEpochSecond(now + (long) metalFullSeconds)));
    }

    double crystalFullSeconds = (capacity.getCrystal() - resources.getCrystal()) * 3600.0 / crystal;
    if (Double.isFinite(crystalFullSeconds) && crystalFullSeconds > 0.0) {
      model.addAttribute("crystalFullAt", Date.from(Instant.ofEpochSecond(now + (long) crystalFullSeconds)));
    }

    double deuteriumFullSeconds = (capacity.getDeuterium() - resources.getDeuterium()) * 3600.0 / deuterium;
    if (Double.isFinite(deuteriumFullSeconds) && deuteriumFullSeconds > 0.0) {
      model.addAttribute("deuteriumFullAt", Date.from(Instant.ofEpochSecond(now + (long) deuteriumFullSeconds)));
    }

    return Utils.getAppropriateView(device, "resources");
  }

  @PostMapping("/resources/set-factors")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String setFactors(@Valid SetProductionFactorsForm form) {
    ProductionFactorsDto factors = new ProductionFactorsDto(form.getMetalMineFactor(), form.getCrystalMineFactor(), form.getDeuteriumSynthesizerFactor(), form.getSolarPlantFactor(), form.getFusionReactorFactor(), form.getSolarSatellitesFactor());
    bodyService.setProductionFactors(form.getBody(), factors);
    return "redirect:/resources?body=" + form.getBody();
  }
}
