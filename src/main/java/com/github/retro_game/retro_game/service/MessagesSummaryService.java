package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.MessagesSummaryDto;

public interface MessagesSummaryService {
  @Activity(bodies = "#bodyId")
  MessagesSummaryDto get(long bodyId);
}
