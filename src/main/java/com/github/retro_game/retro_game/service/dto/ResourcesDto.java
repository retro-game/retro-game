package com.github.retro_game.retro_game.service.dto;

public class ResourcesDto {
  private final double metal;
  private final double crystal;
  private final double deuterium;

  public ResourcesDto(double metal, double crystal, double deuterium) {
    this.metal = metal;
    this.crystal = crystal;
    this.deuterium = deuterium;
  }

  public double getMetal() {
    return metal;
  }

  public double getCrystal() {
    return crystal;
  }

  public double getDeuterium() {
    return deuterium;
  }
}
