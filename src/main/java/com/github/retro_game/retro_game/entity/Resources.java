package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Resources {
  @Column(name = "metal", nullable = false)
  private double metal;

  @Column(name = "crystal", nullable = false)
  private double crystal;

  @Column(name = "deuterium", nullable = false)
  private double deuterium;

  public Resources() {
    metal = 0.0;
    crystal = 0.0;
    deuterium = 0.0;
  }

  public Resources(double metal, double crystal, double deuterium) {
    this.metal = metal;
    this.crystal = crystal;
    this.deuterium = deuterium;
  }

  public Resources(Resources resources) {
    this.metal = resources.metal;
    this.crystal = resources.crystal;
    this.deuterium = resources.deuterium;
  }

  public void add(Resources resources) {
    metal += resources.metal;
    crystal += resources.crystal;
    deuterium += resources.deuterium;
  }

  public void sub(Resources resources) {
    metal -= resources.metal;
    crystal -= resources.crystal;
    deuterium -= resources.deuterium;
  }

  public void mul(double scalar) {
    metal *= scalar;
    crystal *= scalar;
    deuterium *= scalar;
  }

  public void floor() {
    metal = Math.floor(metal);
    crystal = Math.floor(crystal);
    deuterium = Math.floor(deuterium);
  }

  public boolean less(Resources rhs) {
    return metal < rhs.metal && crystal < rhs.crystal && deuterium < rhs.deuterium;
  }

  public boolean greater(Resources rhs) {
    return metal > rhs.metal && crystal > rhs.crystal && deuterium > rhs.deuterium;
  }

  public boolean lessOrEqual(Resources rhs) {
    return metal <= rhs.metal && crystal <= rhs.crystal && deuterium <= rhs.deuterium;
  }

  public boolean greaterOrEqual(Resources rhs) {
    return metal >= rhs.metal && crystal >= rhs.crystal && deuterium >= rhs.deuterium;
  }

  public double getMetal() {
    return metal;
  }

  public void setMetal(double metal) {
    this.metal = metal;
  }

  public double getCrystal() {
    return crystal;
  }

  public void setCrystal(double crystal) {
    this.crystal = crystal;
  }

  public double getDeuterium() {
    return deuterium;
  }

  public void setDeuterium(double deuterium) {
    this.deuterium = deuterium;
  }

  @Override
  public String toString() {
    return "Resources{" +
        "metal=" + metal +
        ", crystal=" + crystal +
        ", deuterium=" + deuterium +
        '}';
  }
}
