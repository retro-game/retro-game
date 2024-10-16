package com.github.retro_game.retro_game.controller.form;

import org.hibernate.validator.constraints.Range;

public class SetProductionFactorsForm {
  private long body;

  @Range(min = 0, max = 10)
  private int metalMineFactor;

  @Range(min = 0, max = 10)
  private int crystalMineFactor;

  @Range(min = 0, max = 10)
  private int deuteriumSynthesizerFactor;

  @Range(min = 0, max = 10)
  private int solarPlantFactor;

  @Range(min = 0, max = 10)
  private int fusionReactorFactor;

  @Range(min = 0, max = 10)
  private int solarSatellitesFactor;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
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
