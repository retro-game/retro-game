package com.github.retro_game.retro_game.dto;

import java.util.Map;

public record BodyContextDto(long id, String name, CoordinatesDto coordinates, BodyTypeDto type, int image,
                             ResourcesDto resources, ProductionDto production, ResourcesDto capacity,
                             Map<BuildingKindDto, Integer> buildings, Map<UnitKindDto, Integer> units) {
}
