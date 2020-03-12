package com.github.retro_game.retro_game.dto;

import java.util.Map;

public class CombatReportRoundCombatantDto {
  private final String name;
  private final Map<UnitKindDto, CombatReportRoundUnitGroupDto> unitGroups;

  public CombatReportRoundCombatantDto(String name, Map<UnitKindDto, CombatReportRoundUnitGroupDto> unitGroups) {
    this.name = name;
    this.unitGroups = unitGroups;
  }

  public String getName() {
    return name;
  }

  public Map<UnitKindDto, CombatReportRoundUnitGroupDto> getUnitGroups() {
    return unitGroups;
  }
}
