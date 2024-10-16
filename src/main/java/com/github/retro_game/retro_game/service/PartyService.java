package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PartyDto;
import com.github.retro_game.retro_game.dto.PartyTargetDto;

import java.util.List;

public interface PartyService {
  PartyDto get(long bodyId, long partyId);

  List<PartyTargetDto> getPartiesTargets(long bodyId);

  long create(long bodyId, long flightId);

  void invite(long bodyId, long partyId, String inviteeName);
}
