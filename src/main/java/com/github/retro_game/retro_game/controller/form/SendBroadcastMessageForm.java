package com.github.retro_game.retro_game.controller.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SendBroadcastMessageForm {
  private long body;

  @NotBlank
  @Size(max = 4095)
  private String message;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
