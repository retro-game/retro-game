package com.github.retro_game.retro_game.controller.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ResetPasswordForm {

  @NotNull
  @Size(min = 8, max = 256)
  private String password;

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
