package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "simplified_combat_reports")
public class SimplifiedCombatReport {
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

  @Column(name = "enemy_id", updatable = false)
  private Long enemyId;

  @Column(name = "enemy_name", nullable = false, updatable = false)
  private String enemyName;

  @Embedded
  private Coordinates coordinates;

  @Column(name = "result", nullable = false, updatable = false)
  private CombatResult result;

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

  @Column(name = "combat_report_id", updatable = false)
  private Long combatReportId;

  @Column(name = "token", updatable = false)
  private byte[] token;

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

  public CombatResult getResult() {
    return result;
  }

  public void setResult(CombatResult result) {
    this.result = result;
  }

  public long getAttackersLoss() {
    return attackersLoss;
  }

  public void setAttackersLoss(long attackersLoss) {
    this.attackersLoss = attackersLoss;
  }

  public Long getDefendersLoss() {
    return defendersLoss;
  }

  public void setDefendersLoss(Long defendersLoss) {
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

  public Long getCombatReportId() {
    return combatReportId;
  }

  public void setCombatReportId(Long combatReportId) {
    this.combatReportId = combatReportId;
  }

  public byte[] getToken() {
    return token;
  }

  public void setToken(byte[] token) {
    this.token = token;
  }
}
