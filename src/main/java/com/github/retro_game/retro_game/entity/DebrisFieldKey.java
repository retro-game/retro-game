package com.github.retro_game.retro_game.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DebrisFieldKey implements Serializable {
  @Column(name = "galaxy", nullable = false, updatable = false)
  @Getter
  private int galaxy;

  @Column(name = "system", nullable = false, updatable = false)
  @Getter
  private int system;

  @Column(name = "position", nullable = false, updatable = false)
  @Getter
  private int position;
}
