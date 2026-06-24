package com.github.retro_game.retro_game.dto;

public class TradeResourcesParamsDto {
  private final long bodyId;
  private final String tradedResourceKey;
  private final double metalAmount;
  private final double crystalAmount;
  private final double deuteriumAmount;
  private final double expectedReceivedAmount;

  public TradeResourcesParamsDto(long bodyId, String tradedResourceKey, double metalAmount, double crystalAmount,
                                 double deuteriumAmount, double expectedReceivedAmount) {
    this.bodyId = bodyId;
    this.tradedResourceKey = tradedResourceKey;
    this.metalAmount = metalAmount;
    this.crystalAmount = crystalAmount;
    this.deuteriumAmount = deuteriumAmount;
    this.expectedReceivedAmount = expectedReceivedAmount;
  }

  public long getBodyId() {
    return bodyId;
  }

  public String getTradedResourceKey() {
    return tradedResourceKey;
  }

  public double getMetalAmount() {
    return metalAmount;
  }

  public double getCrystalAmount() {
    return crystalAmount;
  }

  public double getDeuteriumAmount() {
    return deuteriumAmount;
  }

  public double getExpectedReceivedAmount() {
    return expectedReceivedAmount;
  }
}
