package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.BroadcastMessageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface BroadcastMessageService {
  boolean isCurrentUserAllowedToBroadcastMessage();

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void send(long bodyId, String message);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<BroadcastMessageDto> getMessages(long bodyId, Pageable pageable);
}
