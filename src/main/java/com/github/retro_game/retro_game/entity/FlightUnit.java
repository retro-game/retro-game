package com.github.retro_game.retro_game.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "flight_units")
public class FlightUnit {
  @EmbeddedId
  private FlightUnitKey key;

  @Column(name = "count", nullable = false)
  private int count;

  public FlightUnitKey getKey() {
    return key;
  }

  public void setKey(FlightUnitKey key) {
    this.key = key;
  }

  public Flight getFlight() {
    return key.getFlight();
  }

  public UnitKind getKind() {
    return key.getKind();
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
