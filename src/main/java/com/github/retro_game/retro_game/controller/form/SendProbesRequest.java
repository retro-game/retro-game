package com.github.retro_game.retro_game.controller.form;

import com.github.retro_game.retro_game.service.dto.CoordinatesKindDto;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SendProbesRequest {
  private long body;

  @Range(min = 1, max = 5)
  private int galaxy;

  @Range(min = 1, max = 500)
  private int system;

  @Range(min = 1, max = 15)
  private int position;

  @NotNull
  private CoordinatesKindDto kind;

  @Min(1)
  private int count;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public int getGalaxy() {
    return galaxy;
  }

  public void setGalaxy(int galaxy) {
    this.galaxy = galaxy;
  }

  public int getSystem() {
    return system;
  }

  public void setSystem(int system) {
    this.system = system;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public CoordinatesKindDto getKind() {
    return kind;
  }

  public void setKind(CoordinatesKindDto kind) {
    this.kind = kind;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
