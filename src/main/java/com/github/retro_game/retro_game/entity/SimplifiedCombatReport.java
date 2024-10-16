package com.github.retro_game.retro_game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "simplified_combat_reports")
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedCombatReport {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private long id;

  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  @Getter
  private User user;

  @Column(name = "deleted", nullable = false)
  @Getter
  @Setter
  private boolean deleted;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @Getter
  private Date at;

  @Column(name = "enemy_id", updatable = false)
  @Getter
  private Long enemyId;

  @Column(name = "enemy_name", nullable = false, updatable = false)
  @Getter
  private String enemyName;

  @Embedded
  @Getter
  private Coordinates coordinates;

  @Column(name = "result", nullable = false, updatable = false)
  @Getter
  private CombatResult result;

  @Column(name = "attackers_loss", nullable = false, updatable = false)
  @Getter
  private long attackersLoss;

  @Column(name = "defenders_loss", nullable = false, updatable = false)
  @Getter
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
  @Getter
  private Resources plunder;

  @Column(name = "debris_metal", nullable = false, updatable = false)
  @Getter
  private long debrisMetal;

  @Column(name = "debris_crystal", nullable = false, updatable = false)
  @Getter
  private long debrisCrystal;

  @Column(name = "moon_chance", nullable = false, updatable = false)
  @Getter
  private double moonChance;

  @Column(name = "moon_given", nullable = false, updatable = false)
  @Getter
  private boolean moonGiven;

  @Column(name = "combat_report_id", updatable = false)
  @Getter
  private UUID combatReportId;
}
