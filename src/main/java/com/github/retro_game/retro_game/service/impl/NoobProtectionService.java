package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.service.dto.NoobProtectionRankDto;

interface NoobProtectionService {
  NoobProtectionRankDto getOtherPlayerRank(long selfId, long otherId);
}
