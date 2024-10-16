package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PrangerEntryDto;

import java.util.List;

public interface PrangerService {
  List<PrangerEntryDto> get(long bodyId);
}
