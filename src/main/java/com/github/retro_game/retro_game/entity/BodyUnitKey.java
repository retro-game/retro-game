package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BodyUnitKey implements Serializable {
  @JoinColumn(name = "body_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Body body;

  @Column(name = "kind", nullable = false, updatable = false)
  private UnitKind kind;

  public Body getBody() {
    return body;
  }

  public void setBody(Body body) {
    this.body = body;
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
    BodyUnitKey key = (BodyUnitKey) o;
    return Objects.equals(body, key.body) && kind == key.kind;
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, kind);
  }
}
