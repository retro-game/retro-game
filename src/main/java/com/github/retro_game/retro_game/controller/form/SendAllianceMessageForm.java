package com.github.retro_game.retro_game.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendAllianceMessageForm {
  private long body;
  private long alliance;

  @NotBlank
  @Size(max = 4095)
  private String message;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public long getAlliance() {
    return alliance;
  }

  public void setAlliance(long alliance) {
    this.alliance = alliance;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
