package com.github.retro_game.retro_game.controller;

import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exposes the {@code retro-game.*} configuration values that {@code layout.html}
 * surfaces as {@code data-*} attributes on &lt;body&gt; for the client-side
 * calculators.
 *
 * <p>They are added as plain model attributes rather than read inline with
 * {@code @environment.getProperty(...)} because Thymeleaf 3.1 evaluates the
 * &lt;body&gt; element's attributes (merged by the layout dialect) in a
 * restricted context that forbids reaching into Spring beans from the template.
 */
@ControllerAdvice
public class GameConfigModelAdvice {
  private final Map<String, Object> layoutConfig;

  public GameConfigModelAdvice(Environment environment) {
    layoutConfig = new LinkedHashMap<>();
    layoutConfig.put("productionSpeed", environment.getProperty("retro-game.production-speed"));
    layoutConfig.put("fleetSpeed", environment.getProperty("retro-game.fleet-speed"));
    layoutConfig.put("metalMineBaseProduction", environment.getProperty("retro-game.metal-mine-base-production"));
    layoutConfig.put("crystalMineBaseProduction", environment.getProperty("retro-game.crystal-mine-base-production"));
    layoutConfig.put("deuteriumSynthesizerBaseProduction", environment.getProperty("retro-game.deuterium-synthesizer-base-production"));
    layoutConfig.put("metalMineBaseEnergyUsage", environment.getProperty("retro-game.metal-mine-base-energy-usage"));
    layoutConfig.put("crystalMineBaseEnergyUsage", environment.getProperty("retro-game.crystal-mine-base-energy-usage"));
    layoutConfig.put("deuteriumSynthesizerBaseEnergyUsage", environment.getProperty("retro-game.deuterium-synthesizer-base-energy-usage"));
    layoutConfig.put("solarPlantBaseEnergyProduction", environment.getProperty("retro-game.solar-plant-base-energy-production"));
    layoutConfig.put("fusionReactorBaseEnergyProduction", environment.getProperty("retro-game.fusion-reactor-base-energy-production"));
    layoutConfig.put("fusionReactorBaseDeuteriumUsage", environment.getProperty("retro-game.fusion-reactor-base-deuterium-usage"));
    layoutConfig.put("fieldsPerTerraformerLevel", environment.getProperty("retro-game.fields-per-terraformer-level"));
    layoutConfig.put("fieldsPerLunarBaseLevel", environment.getProperty("retro-game.fields-per-lunar-base-level"));
    layoutConfig.put("storageCapacityMultiplier", environment.getProperty("retro-game.storage-capacity-multiplier"));
  }

  @ModelAttribute
  public void addLayoutConfig(Model model) {
    model.addAllAttributes(layoutConfig);
  }
}
