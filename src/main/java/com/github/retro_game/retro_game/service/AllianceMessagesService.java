package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.AllianceMessageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface AllianceMessagesService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void send(long bodyId, long allianceId, String message);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<AllianceMessageDto> getCurrentUserAllianceMessages(long bodyId, Pageable pageable);
}
