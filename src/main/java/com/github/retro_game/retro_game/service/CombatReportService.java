package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.CombatReportDto;

import java.util.UUID;

public interface CombatReportService {
  CombatReportDto get(UUID id);
}
