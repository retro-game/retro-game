package com.github.retro_game.retro_game.model;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;

// A helper for cost & required energy calculation.
public class ItemCostUtils {
  public static Resources getCost(BuildingKind kind, int level) {
    // When a building of level 1 needs to be destroyed, the passed level is 0.
    assert level >= 0;
    var item = Item.get(kind);
    var cost = item.getBaseCost();
    cost.mul(Math.pow(item.getCostFactor(), level - 1));
    cost.floor();
    return cost;
  }

  public static Resources getCost(TechnologyKind kind, int level) {
    assert level >= 1;
    var item = Item.get(kind);
    // This is the formula for Astrophysics, but can be applied to other technologies as well.
    var cost = item.getBaseCost();
    cost.mul(0.01);
    cost.mul(Math.pow(item.getCostFactor(), level - 1));
    cost.add(new Resources(0.5, 0.5, 0.5));
    cost.floor();
    cost.mul(100.0);
    cost.floor();
    return cost;
  }

  public static int getRequiredEnergy(BuildingKind kind, int level) {
    assert level >= 0;
    var item = Item.get(kind);
    return getRequiredEnergy(item.getBaseRequiredEnergy(), item.getCostFactor(), level);
  }

  public static int getRequiredEnergy(TechnologyKind kind, int level) {
    assert level >= 1;
    var item = Item.get(kind);
    return getRequiredEnergy(item.getBaseRequiredEnergy(), item.getCostFactor(), level);
  }

  private static int getRequiredEnergy(int base, double factor, int level) {
    assert level >= 0;
    return (int) (base * Math.pow(factor, level - 1));
  }
}
