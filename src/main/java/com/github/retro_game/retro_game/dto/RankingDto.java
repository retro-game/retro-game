package com.github.retro_game.retro_game.dto;

import java.util.Date;
import java.util.List;

public class RankingDto {
  private final Date updatedAt;
  private final List<RankingEntryDto> entries;

  public RankingDto(Date updatedAt, List<RankingEntryDto> entries) {
    this.updatedAt = updatedAt;
    this.entries = entries;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public List<RankingEntryDto> getEntries() {
    return entries;
  }
}
