package com.github.retro_game.retro_game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "debris_fields")
@AllArgsConstructor
@NoArgsConstructor
public class DebrisField {
  @EmbeddedId
  private DebrisFieldKey key;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @Getter
  private Date createdAt;

  @Column(name = "updated_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @Getter
  @Setter
  private Date updatedAt;

  @Column(name = "metal", nullable = false)
  @Getter
  @Setter
  private long metal;

  @Column(name = "crystal", nullable = false)
  @Getter
  @Setter
  private long crystal;
}
