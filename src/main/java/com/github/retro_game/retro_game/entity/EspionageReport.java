package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "espionage_reports")
public class EspionageReport {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @JoinColumn(name = "enemy_id", updatable = false)
  private Long enemyId;

  @Column(name = "enemy_name", nullable = false, updatable = false)
  private String enemyName;

  @Embedded
  private Coordinates coordinates;

  @Column(name = "activity", nullable = false, updatable = false)
  private int activity;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "metal", column = @Column(name = "metal", nullable = false, updatable = false)),
      @AttributeOverride(name = "crystal", column = @Column(name = "crystal", nullable = false, updatable = false)),
      @AttributeOverride(name = "deuterium", column = @Column(name = "deuterium", nullable = false, updatable = false)),
  })
  private Resources resources;

  @Column(name = "fleet", updatable = false)
  private Long fleet;

  @Column(name = "defense", updatable = false)
  private Long defense;

  @Column(name = "diameter", nullable = false, updatable = false)
  private int diameter;

  @Column(name = "counter_chance", nullable = false, updatable = false)
  private double counterChance;

  @Column(name = "token", nullable = false, updatable = false)
  private byte[] token;

  @Column(name = "data", nullable = false, updatable = false)
  private byte[] data;

  public long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Date getAt() {
    return at;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public Long getEnemyId() {
    return enemyId;
  }

  public void setEnemyId(Long enemyId) {
    this.enemyId = enemyId;
  }

  public String getEnemyName() {
    return enemyName;
  }

  public void setEnemyName(String enemyName) {
    this.enemyName = enemyName;
  }

  public Coordinates getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  public int getActivity() {
    return activity;
  }

  public void setActivity(int activity) {
    this.activity = activity;
  }

  public Resources getResources() {
    return resources;
  }

  public void setResources(Resources resources) {
    this.resources = resources;
  }

  public Long getFleet() {
    return fleet;
  }

  public void setFleet(Long fleet) {
    this.fleet = fleet;
  }

  public Long getDefense() {
    return defense;
  }

  public void setDefense(Long defense) {
    this.defense = defense;
  }

  public int getDiameter() {
    return diameter;
  }

  public void setDiameter(int diameter) {
    this.diameter = diameter;
  }

  public double getCounterChance() {
    return counterChance;
  }

  public void setCounterChance(double counterChance) {
    this.counterChance = counterChance;
  }

  public byte[] getToken() {
    return token;
  }

  public void setToken(byte[] token) {
    this.token = token;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }
}
