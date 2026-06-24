package com.github.retro_game.retro_game.dto;

public class TradeOfferDto {
  private final String tradedResource;
  private final String tradedResourceKey;
  private final double metalRate;
  private final double crystalRate;
  private final double deuteriumRate;

  public TradeOfferDto(String tradedResource, String tradedResourceKey, double metalRate, double crystalRate,
                       double deuteriumRate) {
    this.tradedResource = tradedResource;
    this.tradedResourceKey = tradedResourceKey;
    this.metalRate = metalRate;
    this.crystalRate = crystalRate;
    this.deuteriumRate = deuteriumRate;
  }

  public String getTradedResource() {
    return tradedResource;
  }

  public String getTradedResourceKey() {
    return tradedResourceKey;
  }

  public double getMetalRate() {
    return metalRate;
  }

  public double getCrystalRate() {
    return crystalRate;
  }

  public double getDeuteriumRate() {
    return deuteriumRate;
  }
}
