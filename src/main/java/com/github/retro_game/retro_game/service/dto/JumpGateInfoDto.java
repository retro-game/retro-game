package com.github.retro_game.retro_game.service.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class JumpGateInfoDto {
  private final Date canJumpAt;
  private final List<JumpGateTargetDto> targets;
  private final Map<UnitKindDto, Integer> units;

  public JumpGateInfoDto(Date canJumpAt, List<JumpGateTargetDto> targets, Map<UnitKindDto, Integer> units) {
    this.canJumpAt = canJumpAt;
    this.targets = targets;
    this.units = units;
  }

  public Date getCanJumpAt() {
    return canJumpAt;
  }

  public List<JumpGateTargetDto> getTargets() {
    return targets;
  }

  public Map<UnitKindDto, Integer> getUnits() {
    return units;
  }
}
