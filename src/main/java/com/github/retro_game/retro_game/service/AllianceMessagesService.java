package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.AllianceMessageDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AllianceMessagesService {
  @Activity(bodies = "#bodyId")
  void send(long bodyId, long allianceId, String message);

  @Activity(bodies = "#bodyId")
  List<AllianceMessageDto> getCurrentUserAllianceMessages(long bodyId, Pageable pageable);
}
