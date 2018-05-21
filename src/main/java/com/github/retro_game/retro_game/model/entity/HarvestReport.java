package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "harvest_reports")
public class HarvestReport {
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

  @Embedded
  private Coordinates coordinates;

  @Column(name = "num_recyclers", nullable = false, updatable = false)
  private int numRecyclers;

  @Column(name = "capacity", nullable = false, updatable = false)
  private long capacity;

  @Column(name = "harvested_metal", nullable = false, updatable = false)
  private long harvestedMetal;

  @Column(name = "harvestedCrystal", nullable = false, updatable = false)
  private long harvestedCrystal;

  @Column(name = "remaining_metal", nullable = false, updatable = false)
  private long remainingMetal;

  @Column(name = "remaining_crystal", nullable = false, updatable = false)
  private long remainingCrystal;

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

  public Coordinates getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  public int getNumRecyclers() {
    return numRecyclers;
  }

  public void setNumRecyclers(int numRecyclers) {
    this.numRecyclers = numRecyclers;
  }

  public long getCapacity() {
    return capacity;
  }

  public void setCapacity(long capacity) {
    this.capacity = capacity;
  }

  public long getHarvestedMetal() {
    return harvestedMetal;
  }

  public void setHarvestedMetal(long harvestedMetal) {
    this.harvestedMetal = harvestedMetal;
  }

  public long getHarvestedCrystal() {
    return harvestedCrystal;
  }

  public void setHarvestedCrystal(long harvestedCrystal) {
    this.harvestedCrystal = harvestedCrystal;
  }

  public long getRemainingMetal() {
    return remainingMetal;
  }

  public void setRemainingMetal(long remainingMetal) {
    this.remainingMetal = remainingMetal;
  }

  public long getRemainingCrystal() {
    return remainingCrystal;
  }

  public void setRemainingCrystal(long remainingCrystal) {
    this.remainingCrystal = remainingCrystal;
  }
}
