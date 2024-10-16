package com.github.retro_game.retro_game.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Resources {
  @Column(name = "metal", nullable = false)
  @Getter
  @Setter
  private double metal;

  @Column(name = "crystal", nullable = false)
  @Getter
  @Setter
  private double crystal;

  @Column(name = "deuterium", nullable = false)
  @Getter
  @Setter
  private double deuterium;

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

  public void max(double scalar) {
    metal = Math.max(metal, scalar);
    crystal = Math.max(crystal, scalar);
    deuterium = Math.max(deuterium, scalar);
  }

  public double total() {
    return metal + crystal + deuterium;
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

  public boolean isNonNegative() {
    return metal >= 0.0 && crystal >= 0.0 && deuterium >= 0.0;
  }
}
