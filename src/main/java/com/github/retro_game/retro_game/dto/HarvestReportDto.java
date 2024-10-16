package com.github.retro_game.retro_game.dto;

import java.util.Date;

public class HarvestReportDto {
  private final long id;
  private final Date at;
  private final CoordinatesDto coordinates;
  private final int numRecyclers;
  private final long capacity;
  private final long harvestedMetal;
  private final long harvestedCrystal;
  private final long remainingMetal;
  private final long remainingCrystal;

  public HarvestReportDto(long id, Date at, CoordinatesDto coordinates, int numRecyclers, long capacity,
                          long harvestedMetal, long harvestedCrystal, long remainingMetal, long remainingCrystal) {
    this.id = id;
    this.at = at;
    this.coordinates = coordinates;
    this.numRecyclers = numRecyclers;
    this.capacity = capacity;
    this.harvestedMetal = harvestedMetal;
    this.harvestedCrystal = harvestedCrystal;
    this.remainingMetal = remainingMetal;
    this.remainingCrystal = remainingCrystal;
  }

  public long getId() {
    return id;
  }

  public Date getAt() {
    return at;
  }

  public CoordinatesDto getCoordinates() {
    return coordinates;
  }

  public int getNumRecyclers() {
    return numRecyclers;
  }

  public long getCapacity() {
    return capacity;
  }

  public long getHarvestedMetal() {
    return harvestedMetal;
  }

  public long getHarvestedCrystal() {
    return harvestedCrystal;
  }

  public long getRemainingMetal() {
    return remainingMetal;
  }

  public long getRemainingCrystal() {
    return remainingCrystal;
  }
}
