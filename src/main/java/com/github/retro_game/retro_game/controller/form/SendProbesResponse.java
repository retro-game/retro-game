package com.github.retro_game.retro_game.controller.form;

public class SendProbesResponse {
  private boolean success;
  private String error;

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}