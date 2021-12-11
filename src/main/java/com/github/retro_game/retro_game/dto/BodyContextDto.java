package com.github.retro_game.retro_game.dto;

public record BodyContextDto(long id, String name, CoordinatesDto coordinates, BodyTypeDto type, int image,
                             ResourcesDto resources, ProductionDto production, ResourcesDto capacity) {
}
