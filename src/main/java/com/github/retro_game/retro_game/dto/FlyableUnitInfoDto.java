package com.github.retro_game.retro_game.dto;

public class FlyableUnitInfoDto {
  private final int count;
  private final int capacity;
  private final int consumption;
  private final int speed;
  private final double weapons;
  private final double shield;
  private final double armor;

  public FlyableUnitInfoDto(int count, int capacity, int consumption, int speed, double weapons, double shield,
                            double armor) {
    this.count = count;
    this.capacity = capacity;
    this.consumption = consumption;
    this.speed = speed;
    this.weapons = weapons;
    this.shield = shield;
    this.armor = armor;
  }

  public int getCount() {
    return count;
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

  public double getWeapons() {
    return weapons;
  }

  public double getShield() {
    return shield;
  }

  public double getArmor() {
    return armor;
  }
}
