package com.github.retro_game.retro_game.dto;

import java.util.Map;

public class CombatReportCombatantDto {
  private final String name;
  private final CoordinatesDto coordinates;
  private final int weaponsTechnology;
  private final int shieldingTechnology;
  private final int armorTechnology;
  private final Map<UnitKindDto, CombatReportUnitGroupDto> unitGroups;

  public CombatReportCombatantDto(String name, CoordinatesDto coordinates, int weaponsTechnology,
                                  int shieldingTechnology, int armorTechnology,
                                  Map<UnitKindDto, CombatReportUnitGroupDto> unitGroups) {
    this.name = name;
    this.coordinates = coordinates;
    this.weaponsTechnology = weaponsTechnology;
    this.shieldingTechnology = shieldingTechnology;
    this.armorTechnology = armorTechnology;
    this.unitGroups = unitGroups;
  }

  public String getName() {
    return name;
  }

  public CoordinatesDto getCoordinates() {
    return coordinates;
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
