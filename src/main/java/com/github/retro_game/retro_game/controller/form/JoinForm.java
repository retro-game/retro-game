package com.github.retro_game.retro_game.controller.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class JoinForm {
  @NotNull
  @Size(min = 3, max = 128)
  @Email
  private String email;

  @NotNull
  @Size(min = 3, max = 16)
  @Pattern(regexp = "^[A-Za-z0-9]+( ?[A-Za-z0-9])*$")
  private String name;

  @NotNull
  @Size(min = 8, max = 256)
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\"#$%&'()*+,\\-.:;<=>?@^_`|~])[A-Za-z\\d!\"#$%&'()*+,\\-.:;<=>?@^_`|~]+$")
  private String password;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
