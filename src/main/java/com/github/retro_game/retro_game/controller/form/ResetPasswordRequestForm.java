package com.github.retro_game.retro_game.controller.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequestForm {
  @NotNull
  @Size(min = 3, max = 128)
  @Email
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
