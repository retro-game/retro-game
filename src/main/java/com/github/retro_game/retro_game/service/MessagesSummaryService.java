package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.MessagesSummaryDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface MessagesSummaryService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  MessagesSummaryDto get(long bodyId);
}
