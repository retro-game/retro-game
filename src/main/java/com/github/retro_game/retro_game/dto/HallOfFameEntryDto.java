package com.github.retro_game.retro_game.dto;

import java.util.ArrayList;
import java.util.UUID;

public record HallOfFameEntryDto(int rank, ArrayList<Long> attackers, ArrayList<Long> defenders, BattleResultDto result,
                                 long attackersLoss, long defendersLoss, ResourcesDto plunder, ResourcesDto debris,
                                 UUID id) {
  public long totalLoss() {
    return attackersLoss + defendersLoss;
  }
}
