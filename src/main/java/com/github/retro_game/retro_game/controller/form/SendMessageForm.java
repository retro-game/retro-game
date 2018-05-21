package com.github.retro_game.retro_game.controller.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SendMessageForm {
  private long recipient;

  @NotBlank
  @Size(max = 4095)
  private String message;

  public long getRecipient() {
    return recipient;
  }

  public void setRecipient(long recipient) {
    this.recipient = recipient;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
