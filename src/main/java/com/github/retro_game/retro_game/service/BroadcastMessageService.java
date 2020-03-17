package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.BroadcastMessageDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BroadcastMessageService {
  boolean isCurrentUserAllowedToBroadcastMessage();

  @Activity(bodies = "#bodyId")
  void send(long bodyId, String message);

  @Activity(bodies = "#bodyId")
  List<BroadcastMessageDto> getMessages(long bodyId, Pageable pageable);
}
