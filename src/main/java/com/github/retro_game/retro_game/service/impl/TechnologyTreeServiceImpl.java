package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.TechnologyKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.service.TechnologyTreeService;
import com.github.retro_game.retro_game.service.dto.*;
import com.github.retro_game.retro_game.service.impl.item.building.BuildingItem;
import com.github.retro_game.retro_game.service.impl.item.technology.TechnologyItem;
import com.github.retro_game.retro_game.service.impl.item.unit.UnitItem;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Service
class TechnologyTreeServiceImpl implements TechnologyTreeService {
  private static final TechnologyTreeDto technologyTree;

  static {
    Map<BuildingKindDto, RequirementsDto> buildings = new EnumMap<>(BuildingKindDto.class);
    for (Map.Entry<BuildingKind, BuildingItem> entry : BuildingItem.getAll().entrySet()) {
      BuildingItem item = entry.getValue();
      buildings.put(Converter.convert(entry.getKey()), new RequirementsDto(
          Converter.convertBuildingsRequirements(item.getBuildingsRequirements()),
          Converter.convertTechnologiesRequirements(item.getTechnologiesRequirements())));
    }

    Map<TechnologyKindDto, RequirementsDto> technologies = new EnumMap<>(TechnologyKindDto.class);
    for (Map.Entry<TechnologyKind, TechnologyItem> entry : TechnologyItem.getAll().entrySet()) {
      TechnologyItem item = entry.getValue();
      technologies.put(Converter.convert(entry.getKey()), new RequirementsDto(
          Converter.convertBuildingsRequirements(item.getBuildingsRequirements()),
          Converter.convertTechnologiesRequirements(item.getTechnologiesRequirements())));
    }

    Map<UnitKindDto, RequirementsDto> fleet = new EnumMap<>(UnitKindDto.class);
    for (Map.Entry<UnitKind, UnitItem> entry : UnitItem.getFleet().entrySet()) {
      UnitItem item = entry.getValue();
      fleet.put(Converter.convert(entry.getKey()), new RequirementsDto(
          Converter.convertBuildingsRequirements(item.getBuildingsRequirements()),
          Converter.convertTechnologiesRequirements(item.getTechnologiesRequirements())));
    }

    Map<UnitKindDto, RequirementsDto> defense = new EnumMap<>(UnitKindDto.class);
    for (Map.Entry<UnitKind, UnitItem> entry : UnitItem.getDefense().entrySet()) {
      UnitItem item = entry.getValue();
      defense.put(Converter.convert(entry.getKey()), new RequirementsDto(
          Converter.convertBuildingsRequirements(item.getBuildingsRequirements()),
          Converter.convertTechnologiesRequirements(item.getTechnologiesRequirements())));
    }

    technologyTree = new TechnologyTreeDto(
        Collections.unmodifiableMap(buildings),
        Collections.unmodifiableMap(technologies),
        Collections.unmodifiableMap(fleet),
        Collections.unmodifiableMap(defense));
  }

  @Override
  public TechnologyTreeDto getTechnologyTree(long bodyId) {
    return technologyTree;
  }
}
