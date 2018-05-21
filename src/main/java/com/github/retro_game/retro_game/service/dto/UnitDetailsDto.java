package com.github.retro_game.retro_game.service.dto;

import java.util.Map;

public class UnitDetailsDto {
  private final double weapons;
  private final double shield;
  private final double armor;
  private final int capacity;
  private final int consumption;
  private final int speed;
  private final double baseWeapons;
  private final double baseShield;
  private final double baseArmor;
  private final int baseSpeed;
  private final Map<UnitKindDto, Integer> rapidFireAgainst;
  private final Map<UnitKindDto, Integer> rapidFireFrom;

  public UnitDetailsDto(double weapons, double shield, double armor, int capacity, int consumption, int speed,
                        double baseWeapons, double baseShield, double baseArmor, int baseSpeed,
                        Map<UnitKindDto, Integer> rapidFireAgainst, Map<UnitKindDto, Integer> rapidFireFrom) {
    this.weapons = weapons;
    this.shield = shield;
    this.armor = armor;
    this.capacity = capacity;
    this.consumption = consumption;
    this.speed = speed;
    this.baseWeapons = baseWeapons;
    this.baseShield = baseShield;
    this.baseArmor = baseArmor;
    this.baseSpeed = baseSpeed;
    this.rapidFireAgainst = rapidFireAgainst;
    this.rapidFireFrom = rapidFireFrom;
  }

  public double getWeapons() {
    return weapons;
  }

  public double getShield() {
    return shield;
  }

  public double getArmor() {
    return armor;
  }

  public int getCapacity() {
    return capacity;
  }

  public int getConsumption() {
    return consumption;
  }

  public int getSpeed() {
    return speed;
  }

  public double getBaseWeapons() {
    return baseWeapons;
  }

  public double getBaseShield() {
    return baseShield;
  }

  public double getBaseArmor() {
    return baseArmor;
  }

  public int getBaseSpeed() {
    return baseSpeed;
  }

  public Map<UnitKindDto, Integer> getRapidFireAgainst() {
    return rapidFireAgainst;
  }

  public Map<UnitKindDto, Integer> getRapidFireFrom() {
    return rapidFireFrom;
  }
}
