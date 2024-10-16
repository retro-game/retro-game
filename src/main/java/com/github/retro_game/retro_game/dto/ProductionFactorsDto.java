package com.github.retro_game.retro_game.dto;

public class ProductionFactorsDto {
  private final int metalMineFactor;
  private final int crystalMineFactor;
  private final int deuteriumSynthesizerFactor;
  private final int solarPlantFactor;
  private final int fusionReactorFactor;
  private final int solarSatellitesFactor;

  public ProductionFactorsDto(int metalMineFactor, int crystalMineFactor, int deuteriumSynthesizerFactor,
                              int solarPlantFactor, int fusionReactorFactor, int solarSatellitesFactor) {
    this.metalMineFactor = metalMineFactor;
    this.crystalMineFactor = crystalMineFactor;
    this.deuteriumSynthesizerFactor = deuteriumSynthesizerFactor;
    this.solarPlantFactor = solarPlantFactor;
    this.fusionReactorFactor = fusionReactorFactor;
    this.solarSatellitesFactor = solarSatellitesFactor;
  }

  public int getMetalMineFactor() {
    return metalMineFactor;
  }

  public int getCrystalMineFactor() {
    return crystalMineFactor;
  }

  public int getDeuteriumSynthesizerFactor() {
    return deuteriumSynthesizerFactor;
  }

  public int getSolarPlantFactor() {
    return solarPlantFactor;
  }

  public int getFusionReactorFactor() {
    return fusionReactorFactor;
  }

  public int getSolarSatellitesFactor() {
    return solarSatellitesFactor;
  }
}
