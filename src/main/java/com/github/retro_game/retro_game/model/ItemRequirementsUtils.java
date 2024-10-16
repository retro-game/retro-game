package com.github.retro_game.retro_game.model;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.TechnologyKind;
import com.github.retro_game.retro_game.entity.User;

import java.util.Map;

// A helper to check whether the requirements for a given item are met.
public class ItemRequirementsUtils {
  public static boolean meetsBuildingsRequirements(Item item, Map<BuildingKind, Integer> buildings) {
    return item.getBuildingsRequirements().entrySet().stream()
        .allMatch(entry -> buildings.getOrDefault(entry.getKey(), 0) >= entry.getValue());
  }

  public static boolean meetsBuildingsRequirements(Item item, Body body) {
    return item.getBuildingsRequirements().entrySet().stream()
        .allMatch(entry -> body.getBuildingLevel(entry.getKey()) >= entry.getValue());
  }

  public static boolean meetsTechnologiesRequirements(Item item, Map<TechnologyKind, Integer> technologies) {
    return item.getTechnologiesRequirements().entrySet().stream()
        .allMatch(entry -> technologies.getOrDefault(entry.getKey(), 0) >= entry.getValue());
  }

  public static boolean meetsTechnologiesRequirements(Item item, User user) {
    return item.getTechnologiesRequirements().entrySet().stream()
        .allMatch(entry -> user.getTechnologyLevel(entry.getKey()) >= entry.getValue());
  }

  public static boolean meetsRequirements(Item item, Body body) {
    return meetsBuildingsRequirements(item, body) && meetsTechnologiesRequirements(item, body.getUser());
  }
}
