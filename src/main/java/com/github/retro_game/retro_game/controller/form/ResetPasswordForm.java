package com.github.retro_game.retro_game.controller.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
