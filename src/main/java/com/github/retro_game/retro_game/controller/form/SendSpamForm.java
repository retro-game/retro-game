package com.github.retro_game.retro_game.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendSpamForm {
  @NotBlank
  @Size(max = 4095)
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
