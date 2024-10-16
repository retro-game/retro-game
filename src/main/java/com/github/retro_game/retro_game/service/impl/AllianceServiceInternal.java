package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.service.AllianceService;

interface AllianceServiceInternal extends AllianceService {
  UserAndAllianceAndMemberTuple getUserAndAllianceAndMember(long allianceId);
}
