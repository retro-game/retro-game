package com.github.retro_game.retro_game.controller.form;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class TradeForm {
  private long body;

  @NotBlank
  private String tradedResourceKey;

  @Min(0)
  private double expectedReceivedAmount;

  @Min(0)
  private double metalAmount;

  @Min(0)
  private double crystalAmount;

  @Min(0)
  private double deuteriumAmount;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public String getTradedResourceKey() {
    return tradedResourceKey;
  }

  public void setTradedResourceKey(String tradedResourceKey) {
    this.tradedResourceKey = tradedResourceKey;
  }

  public double getExpectedReceivedAmount() {
    return expectedReceivedAmount;
  }

  public void setExpectedReceivedAmount(double expectedReceivedAmount) {
    this.expectedReceivedAmount = expectedReceivedAmount;
  }

  public double getMetalAmount() {
    return metalAmount;
  }

  public void setMetalAmount(double metalAmount) {
    this.metalAmount = metalAmount;
  }

  public double getCrystalAmount() {
    return crystalAmount;
  }

  public void setCrystalAmount(double crystalAmount) {
    this.crystalAmount = crystalAmount;
  }

  public double getDeuteriumAmount() {
    return deuteriumAmount;
  }

  public void setDeuteriumAmount(double deuteriumAmount) {
    this.deuteriumAmount = deuteriumAmount;
  }
}
