package com.github.retro_game.retro_game.model;

import com.github.retro_game.retro_game.entity.Coordinates;

public class ItemUtils {
  // TODO: Move this to config.
  private static final int NUM_SYSTEMS = 500;

  public static boolean isWithinMissilesRange(Coordinates start, int targetGalaxy, int targetSystem, int impulseDriveLevel) {
    assert impulseDriveLevel >= 0;
    if (start.getGalaxy() != targetGalaxy) return false;
    var range = 5 * impulseDriveLevel - 1;
    var diff = Math.abs(start.getSystem() - targetSystem);
    return Math.abs(Math.min(diff, NUM_SYSTEMS - diff)) <= range;
  }

  public static boolean isWithinMissilesRange(Coordinates start, Coordinates target, int impulseDriveLevel) {
    return isWithinMissilesRange(start, target.getGalaxy(), target.getSystem(), impulseDriveLevel);
  }

  public static boolean isWithinPhalanxRange(Coordinates start, int targetGalaxy, int targetSystem, int phalanxLevel) {
    assert phalanxLevel >= 0;
    if (start.getGalaxy() != targetGalaxy) return false;
    var range = phalanxLevel * phalanxLevel - 1;
    var diff = Math.abs(start.getSystem() - targetSystem);
    return Math.abs(Math.min(diff, NUM_SYSTEMS - diff)) <= range;
  }

  public static boolean isWithinPhalanxRange(Coordinates start, Coordinates target, int phalanxLevel) {
    return isWithinPhalanxRange(start, target.getGalaxy(), target.getSystem(), phalanxLevel);
  }
}
