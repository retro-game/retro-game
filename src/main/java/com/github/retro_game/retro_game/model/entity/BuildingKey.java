package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BuildingKey implements Serializable {
  @JoinColumn(name = "body_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Body body;

  @Column(name = "kind", nullable = false, updatable = false)
  private BuildingKind kind;

  public Body getBody() {
    return body;
  }

  public void setBody(Body body) {
    this.body = body;
  }

  public BuildingKind getKind() {
    return kind;
  }

  public void setKind(BuildingKind kind) {
    this.kind = kind;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BuildingKey that = (BuildingKey) o;
    return Objects.equals(body, that.body) && kind == that.kind;
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, kind);
  }
}
