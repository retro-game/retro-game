package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.PartyDto;
import com.github.retro_game.retro_game.service.dto.PartyTargetDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface PartyService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  PartyDto get(long bodyId, long partyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<PartyTargetDto> getPartiesTargets(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  long create(long bodyId, long flightId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void invite(long bodyId, long partyId, String inviteeName);
}
