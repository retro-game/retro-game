package com.github.retro_game.retro_game.service.dto;

import io.vavr.Tuple2;

import java.util.Map;

public class EmpireBodyDto {
  private final long id;
  private final String name;
  private final CoordinatesDto coordinates;
  private final BodyTypeDto type;
  private final int image;
  private final int diameter;
  private final int usedFields;
  private final int maxFields;
  private final int temperature;
  private final Tuple2<ResourcesDto, Long> resources;
  private final int availableEnergy;
  private final int totalEnergy;
  private final Tuple2<ResourcesDto, Long> productionHourly;
  private final Tuple2<ResourcesDto, Long> productionDaily;
  private final Tuple2<ResourcesDto, Long> productionWeekly;
  private final Tuple2<ResourcesDto, Long> production30days;
  private final Tuple2<ResourcesDto, Long> capacity;
  private final Map<BuildingKindDto, Tuple2<Integer, Integer>> buildings;
  private final Map<UnitKindDto, Tuple2<Integer, Integer>> units;

  public EmpireBodyDto(long id, String name, CoordinatesDto coordinates, BodyTypeDto type, int image, int diameter,
                       int usedFields, int maxFields, int temperature, Tuple2<ResourcesDto, Long> resources,
                       int availableEnergy, int totalEnergy, Tuple2<ResourcesDto, Long> productionHourly,
                       Tuple2<ResourcesDto, Long> productionDaily, Tuple2<ResourcesDto, Long> productionWeekly,
                       Tuple2<ResourcesDto, Long> production30days, Tuple2<ResourcesDto, Long> capacity,
                       Map<BuildingKindDto, Tuple2<Integer, Integer>> buildings,
                       Map<UnitKindDto, Tuple2<Integer, Integer>> units) {
    this.id = id;
    this.name = name;
    this.coordinates = coordinates;
    this.type = type;
    this.image = image;
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

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public CoordinatesDto getCoordinates() {
    return coordinates;
  }

  public BodyTypeDto getType() {
    return type;
  }

  public int getImage() {
    return image;
  }

  public int getDiameter() {
    return diameter;
  }

  public int getUsedFields() {
    return usedFields;
  }

  public int getMaxFields() {
    return maxFields;
  }

  public int getTemperature() {
    return temperature;
  }

  public Tuple2<ResourcesDto, Long> getResources() {
    return resources;
  }

  public int getAvailableEnergy() {
    return availableEnergy;
  }

  public int getTotalEnergy() {
    return totalEnergy;
  }

  public Tuple2<ResourcesDto, Long> getProductionHourly() {
    return productionHourly;
  }

  public Tuple2<ResourcesDto, Long> getProductionDaily() {
    return productionDaily;
  }

  public Tuple2<ResourcesDto, Long> getProductionWeekly() {
    return productionWeekly;
  }

  public Tuple2<ResourcesDto, Long> getProduction30days() {
    return production30days;
  }

  public Tuple2<ResourcesDto, Long> getCapacity() {
    return capacity;
  }

  public Map<BuildingKindDto, Tuple2<Integer, Integer>> getBuildings() {
    return buildings;
  }

  public Map<UnitKindDto, Tuple2<Integer, Integer>> getUnits() {
    return units;
  }
}
