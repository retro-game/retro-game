package com.github.retro_game.retro_game.controller.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class InviteToPartyForm {
  private long body;

  private long party;

  @NotNull
  @Size(min = 3, max = 16)
  @Pattern(regexp = "^[A-Za-z0-9]+( ?[A-Za-z0-9])*$")
  private String name;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public long getParty() {
    return party;
  }

  public void setParty(long party) {
    this.party = party;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
