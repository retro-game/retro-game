package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.CombatReportSortOrderDto;
import com.github.retro_game.retro_game.dto.HallOfFameEntryDto;

import java.util.ArrayList;

public interface HallOfFameService {
  ArrayList<HallOfFameEntryDto> get(CombatReportSortOrderDto sortOrder);
}
