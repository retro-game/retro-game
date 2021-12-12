package com.github.retro_game.retro_game.model;

import com.github.retro_game.retro_game.entity.Coordinates;

public class ItemUtils {
  public static boolean isWithinPhalanxRange(Coordinates start, int targetGalaxy, int targetSystem, int phalanxLevel) {
    assert phalanxLevel >= 0;
    if (start.getGalaxy() != targetGalaxy)
      return false;
    var range = phalanxLevel * phalanxLevel - 1;
    var diff = Math.abs(start.getSystem() - targetSystem);
    return Math.abs(Math.min(diff, 500 - diff)) <= range;
  }

  public static boolean isWithinPhalanxRange(Coordinates start, Coordinates target, int phalanxLevel) {
    return isWithinPhalanxRange(start, target.getGalaxy(), target.getSystem(), phalanxLevel);
  }
}
