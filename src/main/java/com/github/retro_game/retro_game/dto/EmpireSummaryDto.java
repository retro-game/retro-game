package com.github.retro_game.retro_game.dto;

import io.vavr.Tuple2;

import java.util.Map;

public class EmpireSummaryDto<T> {
  private final T diameter;
  private final T usedFields;
  private final T maxFields;
  private final T temperature;
  private final Tuple2<ResourcesDto, T> resources;
  private final T availableEnergy;
  private final T totalEnergy;
  private final Tuple2<ResourcesDto, T> productionHourly;
  private final Tuple2<ResourcesDto, T> productionDaily;
  private final Tuple2<ResourcesDto, T> productionWeekly;
  private final Tuple2<ResourcesDto, T> production30days;
  private final Tuple2<ResourcesDto, T> capacity;
  private final Map<BuildingKindDto, Tuple2<T, T>> buildings;
  private final Map<UnitKindDto, Tuple2<T, T>> units;

  public EmpireSummaryDto(T diameter, T usedFields, T maxFields, T temperature, Tuple2<ResourcesDto, T> resources,
                          T availableEnergy, T totalEnergy, Tuple2<ResourcesDto, T> productionHourly,
                          Tuple2<ResourcesDto, T> productionDaily, Tuple2<ResourcesDto, T> productionWeekly,
                          Tuple2<ResourcesDto, T> production30days, Tuple2<ResourcesDto, T> capacity,
                          Map<BuildingKindDto, Tuple2<T, T>> buildings, Map<UnitKindDto, Tuple2<T, T>> units) {
    this.diameter = diameter;
    this.usedFields = usedFields;
    this.maxFields = maxFields;
    this.temperature = temperature;
    this.resources = resources;
    this.availableEnergy = availableEnergy;
    this.totalEnergy = totalEnergy;
    this.productionHourly = productionHourly;
    this.productionDaily = productionDaily;
    this.productionWeekly = productionWeekly;
    this.production30days = production30days;
    this.capacity = capacity;
    this.buildings = buildings;
    this.units = units;
  }

  public T getDiameter() {
    return diameter;
  }

  public T getUsedFields() {
    return usedFields;
  }

  public T getMaxFields() {
    return maxFields;
  }

  public T getTemperature() {
    return temperature;
  }

  public Tuple2<ResourcesDto, T> getResources() {
    return resources;
  }

  public T getAvailableEnergy() {
    return availableEnergy;
  }

  public T getTotalEnergy() {
    return totalEnergy;
  }

  public Tuple2<ResourcesDto, T> getProductionHourly() {
    return productionHourly;
  }

  public Tuple2<ResourcesDto, T> getProductionDaily() {
    return productionDaily;
  }

  public Tuple2<ResourcesDto, T> getProductionWeekly() {
    return productionWeekly;
  }

  public Tuple2<ResourcesDto, T> getProduction30days() {
    return production30days;
  }

  public Tuple2<ResourcesDto, T> getCapacity() {
    return capacity;
  }

  public Map<BuildingKindDto, Tuple2<T, T>> getBuildings() {
    return buildings;
  }

  public Map<UnitKindDto, Tuple2<T, T>> getUnits() {
    return units;
  }
}
