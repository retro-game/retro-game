package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FlightUnitKey implements Serializable {
  @JoinColumn(name = "flight_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Flight flight;

  @Column(name = "kind", nullable = false, updatable = false)
  private UnitKind kind;

  public Flight getFlight() {
    return flight;
  }

  public void setFlight(Flight flight) {
    this.flight = flight;
  }

  public UnitKind getKind() {
    return kind;
  }

  public void setKind(UnitKind kind) {
    this.kind = kind;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FlightUnitKey key = (FlightUnitKey) o;
    return Objects.equals(flight, key.flight) && kind == key.kind;
  }

  @Override
  public int hashCode() {
    return Objects.hash(flight, kind);
  }
}
