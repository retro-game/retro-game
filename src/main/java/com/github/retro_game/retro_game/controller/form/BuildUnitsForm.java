package com.github.retro_game.retro_game.controller.form;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.dto.UnitTypeDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class BuildUnitsForm {
  private long body;

  private UnitTypeDto type;

  @NotNull
  private UnitKindDto kind;

  @Min(1)
  private int count;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public UnitTypeDto getType() {
    return type;
  }

  public void setType(UnitTypeDto type) {
    this.type = type;
  }

  public UnitKindDto getKind() {
    return kind;
  }

  public void setKind(UnitKindDto kind) {
    this.kind = kind;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
