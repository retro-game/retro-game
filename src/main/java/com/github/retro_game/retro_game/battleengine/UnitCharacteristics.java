package com.github.retro_game.retro_game.battleengine;

class UnitCharacteristics {
  private final float weapons;
  private final float shield;
  private final float armor;
  private final int[] rapidFire;

  public UnitCharacteristics(float weapons, float shield, float armor, int[] rapidFire) {
    this.weapons = weapons;
    this.shield = shield;
    this.armor = armor;
    this.rapidFire = rapidFire;
  }

  public float getWeapons() {
    return weapons;
  }

  public float getShield() {
    return shield;
  }

  public float getArmor() {
    return armor;
  }

  public int[] getRapidFire() {
    return rapidFire;
  }
}
