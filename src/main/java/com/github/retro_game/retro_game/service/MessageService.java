package com.github.retro_game.retro_game.service;

import org.springframework.security.access.prepost.PreAuthorize;

public interface MessageService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  int getNumNewMessages(long bodyId);
}
