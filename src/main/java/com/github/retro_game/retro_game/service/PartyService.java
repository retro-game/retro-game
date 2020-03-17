package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PartyDto;
import com.github.retro_game.retro_game.dto.PartyTargetDto;

import java.util.List;

public interface PartyService {
  @Activity(bodies = "#bodyId")
  PartyDto get(long bodyId, long partyId);

  @Activity(bodies = "#bodyId")
  List<PartyTargetDto> getPartiesTargets(long bodyId);

  @Activity(bodies = "#bodyId")
  long create(long bodyId, long flightId);

  @Activity(bodies = "#bodyId")
  void invite(long bodyId, long partyId, String inviteeName);
}
