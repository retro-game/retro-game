package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.TradeOfferDto;
import com.github.retro_game.retro_game.dto.TradeResourcesParamsDto;

public interface TraderService {
  TradeOfferDto getCurrentOffer();

  void trade(TradeResourcesParamsDto params);
}
