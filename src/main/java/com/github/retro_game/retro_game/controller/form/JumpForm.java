package com.github.retro_game.retro_game.controller.form;

import com.github.retro_game.retro_game.dto.UnitKindDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class JumpForm {
  private long body;

  private long target;

  @NotNull
  @NotEmpty
  private Map<UnitKindDto, Integer> units;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public long getTarget() {
    return target;
  }

  public void setTarget(long target) {
    this.target = target;
  }

  public Map<UnitKindDto, Integer> getUnits() {
    return units;
  }

  public void setUnits(Map<UnitKindDto, Integer> units) {
    this.units = units;
  }
}
