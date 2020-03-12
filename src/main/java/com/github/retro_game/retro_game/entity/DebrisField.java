package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "debris_fields")
public class DebrisField {
  @EmbeddedId
  private DebrisFieldKey key;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  @Column(name = "updated_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedAt;

  @Column(name = "metal", nullable = false)
  private long metal;

  @Column(name = "crystal", nullable = false)
  private long crystal;

  public DebrisFieldKey getKey() {
    return key;
  }

  public void setKey(DebrisFieldKey key) {
    this.key = key;
  }

  public int getGalaxy() {
    return key.getGalaxy();
  }

  public int getSystem() {
    return key.getSystem();
  }

  public int getSlot() {
    return key.getPosition();
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public long getMetal() {
    return metal;
  }

  public void setMetal(long metal) {
    this.metal = metal;
  }

  public long getCrystal() {
    return crystal;
  }

  public void setCrystal(long crystal) {
    this.crystal = crystal;
  }
}
