package com.github.retro_game.retro_game.entity;

import com.vladmihalcea.hibernate.type.array.LongArrayType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "combat_reports")
@TypeDef(name = "long-array", typeClass = LongArrayType.class)
@NoArgsConstructor
@AllArgsConstructor
public class CombatReport {
  @Column(name = "id")
  @Id
  @Type(type = "pg-uuid")
  @Getter
  private UUID id;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @Getter
  private Date at;

  @Column(name = "attackers", nullable = false, updatable = false)
  @Type(type = "long-array")
  @Getter
  private long[] attackers;

  @Column(name = "defenders", nullable = false, updatable = false)
  @Type(type = "long-array")
  @Getter
  private long[] defenders;

  @Column(name = "result", nullable = false, updatable = false)
  @Getter
  private BattleResult result;

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

  @Column(name = "seed", nullable = false, updatable = false)
  @Getter
  private int seed;

  @Column(name = "execution_time", nullable = false, updatable = false)
  @Getter
  private long executionTime;

  @Column(name = "data", nullable = false, updatable = false)
  @Getter
  private byte[] data;
}
