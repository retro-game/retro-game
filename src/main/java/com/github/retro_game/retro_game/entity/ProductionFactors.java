package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProductionFactors {
  @Column(name = "metal_mine_factor", nullable = false)
  private int metalMineFactor;

  @Column(name = "crystal_mine_factor", nullable = false)
  private int crystalMineFactor;

  @Column(name = "deuterium_synthesizer_factor", nullable = false)
  private int deuteriumSynthesizerFactor;

  @Column(name = "solar_plant_factor", nullable = false)
  private int solarPlantFactor;

  @Column(name = "fusion_reactor_factor", nullable = false)
  private int fusionReactorFactor;

  @Column(name = "solar_satellites_factor", nullable = false)
  private int solarSatellitesFactor;

  public ProductionFactors() {
    this(10, 10, 10, 10, 10, 10);
  }

  public ProductionFactors(int metalMineFactor, int crystalMineFactor, int deuteriumSynthesizerFactor,
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

  public void setMetalMineFactor(int metalMineFactor) {
    this.metalMineFactor = metalMineFactor;
  }

  public int getCrystalMineFactor() {
    return crystalMineFactor;
  }

  public void setCrystalMineFactor(int crystalMineFactor) {
    this.crystalMineFactor = crystalMineFactor;
  }

  public int getDeuteriumSynthesizerFactor() {
    return deuteriumSynthesizerFactor;
  }

  public void setDeuteriumSynthesizerFactor(int deuteriumSynthesizerFactor) {
    this.deuteriumSynthesizerFactor = deuteriumSynthesizerFactor;
  }

  public int getSolarPlantFactor() {
    return solarPlantFactor;
  }

  public void setSolarPlantFactor(int solarPlantFactor) {
    this.solarPlantFactor = solarPlantFactor;
  }

  public int getFusionReactorFactor() {
    return fusionReactorFactor;
  }

  public void setFusionReactorFactor(int fusionReactorFactor) {
    this.fusionReactorFactor = fusionReactorFactor;
  }

  public int getSolarSatellitesFactor() {
    return solarSatellitesFactor;
  }

  public void setSolarSatellitesFactor(int solarSatellitesFactor) {
    this.solarSatellitesFactor = solarSatellitesFactor;
  }
}
