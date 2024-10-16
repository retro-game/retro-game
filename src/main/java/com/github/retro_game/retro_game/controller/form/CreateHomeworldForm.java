package com.github.retro_game.retro_game.controller.form;

import org.hibernate.validator.constraints.Range;

public class CreateHomeworldForm {
  @Range(min = 1, max = 5)
  private int galaxy;

  @Range(min = 1, max = 500)
  private int system;

  @Range(min = 4, max = 12)
  private int position;

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
}
