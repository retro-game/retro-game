package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "combat_reports")
public class CombatReport {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "token", nullable = false, updatable = false)
  private byte[] token;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @Column(name = "result", nullable = false, updatable = false)
  private BattleResult result;

  @Column(name = "attackers_loss", nullable = false, updatable = false)
  private long attackersLoss;

  @Column(name = "defenders_loss", nullable = false, updatable = false)
  private long defendersLoss;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "metal",
          column = @Column(name = "plunder_metal", nullable = false, updatable = false)),
      @AttributeOverride(name = "crystal",
          column = @Column(name = "plunder_crystal", nullable = false, updatable = false)),
      @AttributeOverride(name = "deuterium",
          column = @Column(name = "plunder_deuterium", nullable = false, updatable = false)),
  })
  private Resources plunder;

  @Column(name = "debris_metal", nullable = false, updatable = false)
  private long debrisMetal;

  @Column(name = "debris_crystal", nullable = false, updatable = false)
  private long debrisCrystal;

  @Column(name = "moon_chance", nullable = false, updatable = false)
  private double moonChance;

  @Column(name = "moon_given", nullable = false, updatable = false)
  private boolean moonGiven;

  @Column(name = "seed", nullable = false, updatable = false)
  private int seed;

  @Column(name = "execution_time", nullable = false, updatable = false)
  private long executionTime;

  @Column(name = "data", nullable = false, updatable = false)
  private byte[] data;

  public long getId() {
    return id;
  }

  public byte[] getToken() {
    return token;
  }

  public void setToken(byte[] token) {
    this.token = token;
  }

  public Date getAt() {
    return at;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public BattleResult getResult() {
    return result;
  }

  public void setResult(BattleResult result) {
    this.result = result;
  }

  public long getAttackersLoss() {
    return attackersLoss;
  }

  public void setAttackersLoss(long attackersLoss) {
    this.attackersLoss = attackersLoss;
  }

  public long getDefendersLoss() {
    return defendersLoss;
  }

  public void setDefendersLoss(long defendersLoss) {
    this.defendersLoss = defendersLoss;
  }

  public Resources getPlunder() {
    return plunder;
  }

  public void setPlunder(Resources plunder) {
    this.plunder = plunder;
  }

  public long getDebrisMetal() {
    return debrisMetal;
  }

  public void setDebrisMetal(long debrisMetal) {
    this.debrisMetal = debrisMetal;
  }

  public long getDebrisCrystal() {
    return debrisCrystal;
  }

  public void setDebrisCrystal(long debrisCrystal) {
    this.debrisCrystal = debrisCrystal;
  }

  public double getMoonChance() {
    return moonChance;
  }

  public void setMoonChance(double moonChance) {
    this.moonChance = moonChance;
  }

  public boolean isMoonGiven() {
    return moonGiven;
  }

  public void setMoonGiven(boolean moonGiven) {
    this.moonGiven = moonGiven;
  }

  public int getSeed() {
    return seed;
  }

  public void setSeed(int seed) {
    this.seed = seed;
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public void setExecutionTime(long executionTime) {
    this.executionTime = executionTime;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }
}
