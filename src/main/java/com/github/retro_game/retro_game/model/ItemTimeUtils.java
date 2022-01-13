package com.github.retro_game.retro_game.model;

import com.github.retro_game.retro_game.dto.ProductionDto;
import com.github.retro_game.retro_game.entity.Resources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

// A helper for time calculation. The methods return the number of seconds required to get an item.
@Component
public class ItemTimeUtils {
  private final int buildingConstructionSpeed;
  private final int buildingDestructionSpeed;
  private final int technologyResearchSpeed;
  private final int unitConstructionSpeed;
  private final double irnBoostFactor;
  private final int minShipyardTime;

  public ItemTimeUtils(@Value("${retro-game.building-construction-speed}") int buildingConstructionSpeed,
                       @Value("${retro-game.building-destruction-speed}") int buildingDestructionSpeed,
                       @Value("${retro-game.technology-research-speed}") int technologyResearchSpeed,
                       @Value("${retro-game.unit-construction-speed}") int unitConstructionSpeed,
                       @Value("${retro-game.irn-boost-factor}") double irnBoostFactor,
                       @Value("${retro-game.min-shipyard-time}") int minShipyardTime) {
    this.buildingConstructionSpeed = buildingConstructionSpeed;
    this.buildingDestructionSpeed = buildingDestructionSpeed;
    this.technologyResearchSpeed = technologyResearchSpeed;
    this.unitConstructionSpeed = unitConstructionSpeed;
    this.irnBoostFactor = irnBoostFactor;
    this.minShipyardTime = minShipyardTime;
  }

  @PostConstruct
  private void checkProperties() {
    Assert.isTrue(buildingConstructionSpeed >= 1,
        "retro-game.building-construction-speed must be at least 1");
    Assert.isTrue(buildingDestructionSpeed >= 1,
        "retro-game.building-destruction-speed must be at least 1");
    Assert.isTrue(technologyResearchSpeed >= 1,
        "retro-game.technology-research-speed must be at least 1");
    Assert.isTrue(unitConstructionSpeed >= 1,
        "retro-game.unit-construction-speed must be at least 1");
    Assert.isTrue(irnBoostFactor >= 0.0,
        "retro-game.irn-boost-factor should be greater than or equal to 0");
    Assert.isTrue(minShipyardTime >= 1,
        "retro-game.min-shipyard-time must be at least 1");
  }

  // Buildings

  public long getBuildingConstructionTime(Resources cost, int roboticsFactoryLevel, int naniteFactoryLevel) {
    return getBuildingTime(cost, roboticsFactoryLevel, naniteFactoryLevel, buildingConstructionSpeed);
  }

  public long getBuildingDestructionTime(Resources cost, int roboticsFactoryLevel, int naniteFactoryLevel) {
    return getBuildingTime(cost, roboticsFactoryLevel, naniteFactoryLevel, buildingDestructionSpeed);
  }

  private static long getBuildingTime(Resources cost, int roboticsFactoryLevel, int naniteFactoryLevel, int speed) {
    assert roboticsFactoryLevel >= 0;
    assert naniteFactoryLevel >= 0;
    assert speed >= 1;
    var seconds = (long) (1.44 * (cost.getMetal() + cost.getCrystal()));
    seconds /= 1 + roboticsFactoryLevel;
    seconds >>= naniteFactoryLevel;
    seconds /= speed;
    return Math.max(1, seconds);
  }

  // Technologies

  public long getTechnologyResearchTime(Resources cost, int effectiveLabLevel, int irnLevel) {
    assert effectiveLabLevel >= 0;
    var seconds = (long) (3.6 * (cost.getMetal() + cost.getCrystal()));
    seconds /= 1 + effectiveLabLevel;
    seconds = (long) (seconds / Math.pow(1 + irnBoostFactor, irnLevel));
    seconds /= technologyResearchSpeed;
    return Math.max(1, seconds);
  }

  // Units

  public long getUnitConstructionTime(Resources cost, int shipyardLevel, int naniteFactoryLevel) {
    assert shipyardLevel >= 0;
    assert naniteFactoryLevel >= 0;
    var ms = 1000L * (long) (1.44 * (cost.getMetal() + cost.getCrystal()));
    ms /= 1 + shipyardLevel;
    ms >>= naniteFactoryLevel;
    ms /= unitConstructionSpeed;
    if (ms >= 1000) {
      return ms / 1000 * 1000;
    }
    return Math.max(minShipyardTime, ms);
  }

  // Other

  public static Long calcAccumulateTime(Resources resources, ProductionDto production) {
    var m = calcAccumulateTime(resources.getMetal(), production.metalProduction());
    var c = calcAccumulateTime(resources.getCrystal(), production.crystalProduction());
    var d = calcAccumulateTime(resources.getDeuterium(), production.deuteriumProduction());
    if (m == null || c == null || d == null) {
      return null;
    }
    return Math.max(Math.max(m, c), d);
  }

  private static Long calcAccumulateTime(double resources, int production) {
    assert production >= 0;
    if (resources > 0 && production == 0) {
      return null;
    }
    return (long) Math.ceil(resources / (production / 3600.0));
  }
}
