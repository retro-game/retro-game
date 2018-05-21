package com.github.retro_game.retro_game.service.dto;

public class ProductionDto {
  private final double efficiency;
  private final int metalBaseProduction;
  private final int crystalBaseProduction;
  private final int deuteriumBaseProduction;
  private final int metalMineProduction;
  private final int metalMineCurrentEnergyUsage;
  private final int metalMineMaxEnergyUsage;
  private final int crystalMineProduction;
  private final int crystalMineCurrentEnergyUsage;
  private final int crystalMineMaxEnergyUsage;
  private final int deuteriumSynthesizerProduction;
  private final int deuteriumSynthesizerCurrentEnergyUsage;
  private final int deuteriumSynthesizerMaxEnergyUsage;
  private final int solarPlantEnergyProduction;
  private final int fusionReactorDeuteriumUsage;
  private final int fusionReactorEnergyProduction;
  private final int solarSatellitesEnergyProduction;
  private final int metalProduction;
  private final int crystalProduction;
  private final int deuteriumProduction;
  private final int totalEnergy;
  private final int usedEnergy;
  private final int availableEnergy;

  public ProductionDto(double efficiency, int metalBaseProduction, int crystalBaseProduction,
                       int deuteriumBaseProduction, int metalMineProduction, int metalMineCurrentEnergyUsage,
                       int metalMineMaxEnergyUsage, int crystalMineProduction, int crystalMineCurrentEnergyUsage,
                       int crystalMineMaxEnergyUsage, int deuteriumSynthesizerProduction,
                       int deuteriumSynthesizerCurrentEnergyUsage, int deuteriumSynthesizerMaxEnergyUsage,
                       int solarPlantEnergyProduction, int fusionReactorDeuteriumUsage,
                       int fusionReactorEnergyProduction, int solarSatellitesEnergyProduction, int metalProduction,
                       int crystalProduction, int deuteriumProduction, int totalEnergy, int usedEnergy,
                       int availableEnergy) {
    this.efficiency = efficiency;
    this.metalBaseProduction = metalBaseProduction;
    this.crystalBaseProduction = crystalBaseProduction;
    this.deuteriumBaseProduction = deuteriumBaseProduction;
    this.metalMineProduction = metalMineProduction;
    this.metalMineCurrentEnergyUsage = metalMineCurrentEnergyUsage;
    this.metalMineMaxEnergyUsage = metalMineMaxEnergyUsage;
    this.crystalMineProduction = crystalMineProduction;
    this.crystalMineCurrentEnergyUsage = crystalMineCurrentEnergyUsage;
    this.crystalMineMaxEnergyUsage = crystalMineMaxEnergyUsage;
    this.deuteriumSynthesizerProduction = deuteriumSynthesizerProduction;
    this.deuteriumSynthesizerCurrentEnergyUsage = deuteriumSynthesizerCurrentEnergyUsage;
    this.deuteriumSynthesizerMaxEnergyUsage = deuteriumSynthesizerMaxEnergyUsage;
    this.solarPlantEnergyProduction = solarPlantEnergyProduction;
    this.fusionReactorDeuteriumUsage = fusionReactorDeuteriumUsage;
    this.fusionReactorEnergyProduction = fusionReactorEnergyProduction;
    this.solarSatellitesEnergyProduction = solarSatellitesEnergyProduction;
    this.metalProduction = metalProduction;
    this.crystalProduction = crystalProduction;
    this.deuteriumProduction = deuteriumProduction;
    this.totalEnergy = totalEnergy;
    this.usedEnergy = usedEnergy;
    this.availableEnergy = availableEnergy;
  }

  public double getEfficiency() {
    return efficiency;
  }

  public int getMetalBaseProduction() {
    return metalBaseProduction;
  }

  public int getCrystalBaseProduction() {
    return crystalBaseProduction;
  }

  public int getDeuteriumBaseProduction() {
    return deuteriumBaseProduction;
  }

  public int getMetalMineProduction() {
    return metalMineProduction;
  }

  public int getMetalMineCurrentEnergyUsage() {
    return metalMineCurrentEnergyUsage;
  }

  public int getMetalMineMaxEnergyUsage() {
    return metalMineMaxEnergyUsage;
  }

  public int getCrystalMineProduction() {
    return crystalMineProduction;
  }

  public int getCrystalMineCurrentEnergyUsage() {
    return crystalMineCurrentEnergyUsage;
  }

  public int getCrystalMineMaxEnergyUsage() {
    return crystalMineMaxEnergyUsage;
  }

  public int getDeuteriumSynthesizerProduction() {
    return deuteriumSynthesizerProduction;
  }

  public int getDeuteriumSynthesizerCurrentEnergyUsage() {
    return deuteriumSynthesizerCurrentEnergyUsage;
  }

  public int getDeuteriumSynthesizerMaxEnergyUsage() {
    return deuteriumSynthesizerMaxEnergyUsage;
  }

  public int getSolarPlantEnergyProduction() {
    return solarPlantEnergyProduction;
  }

  public int getFusionReactorDeuteriumUsage() {
    return fusionReactorDeuteriumUsage;
  }

  public int getFusionReactorEnergyProduction() {
    return fusionReactorEnergyProduction;
  }

  public int getSolarSatellitesEnergyProduction() {
    return solarSatellitesEnergyProduction;
  }

  public int getMetalProduction() {
    return metalProduction;
  }

  public int getCrystalProduction() {
    return crystalProduction;
  }

  public int getDeuteriumProduction() {
    return deuteriumProduction;
  }

  public int getTotalEnergy() {
    return totalEnergy;
  }

  public int getUsedEnergy() {
    return usedEnergy;
  }

  public int getAvailableEnergy() {
    return availableEnergy;
  }
}
