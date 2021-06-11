package com.github.retro_game.retro_game.dto;

public class CombatReportUnitGroupDto {
  private final long numUnits;
  private final double weapons;
  private final double shields;
  private final double armor;

  public CombatReportUnitGroupDto(long numUnits, double weapons, double shields, double armor) {
    this.numUnits = numUnits;
    this.weapons = weapons;
    this.shields = shields;
    this.armor = armor;
  }

  public long getNumUnits() {
    return numUnits;
  }

  public double getWeapons() {
    return weapons;
  }

  public double getShields() {
    return shields;
  }

  public double getArmor() {
    return armor;
  }
}
