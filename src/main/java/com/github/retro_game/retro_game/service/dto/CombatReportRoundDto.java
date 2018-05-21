package com.github.retro_game.retro_game.service.dto;

import java.util.List;

public class CombatReportRoundDto {
  private final List<CombatReportRoundCombatantDto> attackers;
  private final List<CombatReportRoundCombatantDto> defenders;

  public CombatReportRoundDto(List<CombatReportRoundCombatantDto> attackers, List<CombatReportRoundCombatantDto> defenders) {
    this.attackers = attackers;
    this.defenders = defenders;
  }

  public List<CombatReportRoundCombatantDto> getAttackers() {
    return attackers;
  }

  public List<CombatReportRoundCombatantDto> getDefenders() {
    return defenders;
  }
}
