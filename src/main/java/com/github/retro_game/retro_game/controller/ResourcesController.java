package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.SetProductionFactorsForm;
import com.github.retro_game.retro_game.dto.ProductionDto;
import com.github.retro_game.retro_game.dto.ProductionFactorsDto;
import com.github.retro_game.retro_game.dto.ProductionItemsDto;
import com.github.retro_game.retro_game.dto.ResourcesDto;
import com.github.retro_game.retro_game.service.BodyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Date;

@Controller
public class ResourcesController {
  private final BodyService bodyService;

  public ResourcesController(BodyService bodyService) {
    this.bodyService = bodyService;
  }

  @GetMapping("/resources")
  public String resources(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);

    ProductionDto production = bodyService.getProduction(bodyId);
    model.addAttribute("production", production);

    ProductionItemsDto items = bodyService.getProductionItems(bodyId);
    model.addAttribute("items", items);

    ProductionFactorsDto factors = bodyService.getProductionFactors(bodyId);
    model.addAttribute("factors", factors);

    // Economy.

    double metal = production.getMetalProduction();
    double crystal = production.getCrystalProduction();
    double deuterium = production.getDeuteriumProduction();
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

    // Capacity.

    ResourcesDto resources = bodyService.getResources(bodyId);
    ResourcesDto capacity = bodyService.getCapacity(bodyId);
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

    return "resources";
  }

  @PostMapping("/resources/set-factors")
  public String setFactors(@Valid SetProductionFactorsForm form) {
    ProductionFactorsDto factors = new ProductionFactorsDto(form.getMetalMineFactor(), form.getCrystalMineFactor(),
        form.getDeuteriumSynthesizerFactor(), form.getSolarPlantFactor(), form.getFusionReactorFactor(),
        form.getSolarSatellitesFactor());
    bodyService.setProductionFactors(form.getBody(), factors);
    return "redirect:/resources?body=" + form.getBody();
  }
}
