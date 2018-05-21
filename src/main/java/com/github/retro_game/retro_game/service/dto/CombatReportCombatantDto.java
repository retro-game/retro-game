package com.github.retro_game.retro_game.service.dto;

import java.util.Map;

public class CombatReportCombatantDto {
  private final String name;
  private final int weaponsTechnology;
  private final int shieldingTechnology;
  private final int armorTechnology;
  private final Map<UnitKindDto, CombatReportUnitGroupDto> unitGroups;

  public CombatReportCombatantDto(String name, int weaponsTechnology, int shieldingTechnology, int armorTechnology,
                                  Map<UnitKindDto, CombatReportUnitGroupDto> unitGroups) {
    this.name = name;
    this.weaponsTechnology = weaponsTechnology;
    this.shieldingTechnology = shieldingTechnology;
    this.armorTechnology = armorTechnology;
    this.unitGroups = unitGroups;
  }

  public String getName() {
    return name;
  }

  public int getWeaponsTechnology() {
    return weaponsTechnology;
  }

  public int getShieldingTechnology() {
    return shieldingTechnology;
  }

  public int getArmorTechnology() {
    return armorTechnology;
  }

  public Map<UnitKindDto, CombatReportUnitGroupDto> getUnitGroups() {
    return unitGroups;
  }
}
