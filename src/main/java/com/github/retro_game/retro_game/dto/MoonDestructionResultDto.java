package com.github.retro_game.retro_game.dto;

public record MoonDestructionResultDto(double moonDestructionChance, boolean moonDestroyed,
                                       double deathStarsDestructionChance, boolean deathStarsDestroyed) {
}
