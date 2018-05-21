package com.github.retro_game.retro_game.service.impl.item;

import com.github.retro_game.retro_game.model.entity.*;

import java.util.Collections;
import java.util.Map;

public abstract class Item {
  public Map<BuildingKind, Integer> getBuildingsRequirements() {
    return Collections.emptyMap();
  }

  public Map<TechnologyKind, Integer> getTechnologiesRequirements() {
    return Collections.emptyMap();
  }

  // FIXME: Move these to some service. Item package should contain information only.

  public final boolean meetsRequirements(Body body) {
    return meetsBuildingsRequirements(body) && meetsTechnologiesRequirements(body.getUser());
  }

  public boolean meetsBuildingsRequirements(Body body) {
    Map<BuildingKind, Building> buildings = body.getBuildings();
    for (Map.Entry<BuildingKind, Integer> entry : getBuildingsRequirements().entrySet()) {
      Building building = buildings.get(entry.getKey());
      if (building == null || building.getLevel() < entry.getValue()) {
        return false;
      }
    }
    return true;
  }

  public boolean meetsBuildingsRequirements(Map<BuildingKind, Integer> buildings) {
    for (Map.Entry<BuildingKind, Integer> entry : getBuildingsRequirements().entrySet()) {
      if (buildings.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
        return false;
      }
    }
    return true;
  }

  public boolean meetsTechnologiesRequirements(User user) {
    Map<TechnologyKind, Technology> technologies = user.getTechnologies();
    for (Map.Entry<TechnologyKind, Integer> entry : getTechnologiesRequirements().entrySet()) {
      Technology technology = technologies.get(entry.getKey());
      if (technology == null || technology.getLevel() < entry.getValue()) {
        return false;
      }
    }
    return true;
  }

  public boolean meetsTechnologiesRequirements(Map<TechnologyKind, Integer> technologies) {
    return getTechnologiesRequirements().entrySet().stream()
        .allMatch(entry -> technologies.getOrDefault(entry.getKey(), 0) >= entry.getValue());
  }
}
