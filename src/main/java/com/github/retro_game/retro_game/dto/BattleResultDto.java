package com.github.retro_game.retro_game.dto;

public enum BattleResultDto {
  ATTACKERS_WIN, DEFENDERS_WIN, DRAW;

  public boolean isAttackersWin() {
    return this == ATTACKERS_WIN;
  }

  public boolean isDefendersWin() {
    return this == DEFENDERS_WIN;
  }

  public boolean isDraw() {
    return this == DRAW;
  }
}
