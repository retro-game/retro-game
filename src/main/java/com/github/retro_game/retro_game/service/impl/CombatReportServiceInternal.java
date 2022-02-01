package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.battleengine.BattleOutcome;
import com.github.retro_game.retro_game.battleengine.Combatant;
import com.github.retro_game.retro_game.dto.MoonCreationResultDto;
import com.github.retro_game.retro_game.dto.MoonDestructionResultDto;
import com.github.retro_game.retro_game.entity.BattleResult;
import com.github.retro_game.retro_game.entity.CombatReport;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.service.CombatReportService;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

public interface CombatReportServiceInternal extends CombatReportService {
  CombatReport create(Date at, List<Combatant> attackers, List<Combatant> defenders, BattleOutcome battleOutcome,
                      BattleResult result, Resources attackersLoss, Resources defendersLoss, Resources plunder,
                      Resources debris, MoonCreationResultDto moonCreationResult,
                      @Nullable MoonDestructionResultDto moonDestructionResult, int seed, long executionTime);
}
