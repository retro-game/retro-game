package com.github.retro_game.retro_game.model;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.TechnologyKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.model.building.BuildingItem;
import com.github.retro_game.retro_game.model.technology.TechnologyItem;
import com.github.retro_game.retro_game.model.unit.UnitItem;

import java.util.Collections;
import java.util.Map;

public abstract class Item {
  // Gets which buildings with their levels are required to obtain the item.
  public Map<BuildingKind, Integer> getBuildingsRequirements() {
    return Collections.emptyMap();
  }

  // Gets which technologies with their levels are required to obtain the item.
  public Map<TechnologyKind, Integer> getTechnologiesRequirements() {
    return Collections.emptyMap();
  }

  public static BuildingItem get(BuildingKind kind) {
    return BuildingItem.getAll().get(kind);
  }

  public static TechnologyItem get(TechnologyKind kind) {
    return TechnologyItem.getAll().get(kind);
  }

  public static UnitItem get(UnitKind kind) {
    return UnitItem.getAll().get(kind);
  }
}
