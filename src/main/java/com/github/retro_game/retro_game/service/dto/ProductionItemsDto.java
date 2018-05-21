package com.github.retro_game.retro_game.service.dto;

public class ProductionItemsDto {
  private final int metalMineLevel;
  private final int crystalMineLevel;
  private final int deuteriumSynthesizerLevel;
  private final int solarPlantLevel;
  private final int fusionReactorLevel;
  private final int numSolarSatellites;

  public ProductionItemsDto(int metalMineLevel, int crystalMineLevel, int deuteriumSynthesizerLevel,
                            int solarPlantLevel, int fusionReactorLevel, int numSolarSatellites) {
    this.metalMineLevel = metalMineLevel;
    this.crystalMineLevel = crystalMineLevel;
    this.deuteriumSynthesizerLevel = deuteriumSynthesizerLevel;
    this.solarPlantLevel = solarPlantLevel;
    this.fusionReactorLevel = fusionReactorLevel;
    this.numSolarSatellites = numSolarSatellites;
  }

  public int getMetalMineLevel() {
    return metalMineLevel;
  }

  public int getCrystalMineLevel() {
    return crystalMineLevel;
  }

  public int getDeuteriumSynthesizerLevel() {
    return deuteriumSynthesizerLevel;
  }

  public int getSolarPlantLevel() {
    return solarPlantLevel;
  }

  public int getFusionReactorLevel() {
    return fusionReactorLevel;
  }

  public int getNumSolarSatellites() {
    return numSolarSatellites;
  }
}
