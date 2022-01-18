package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.battleengine.BattleOutcome;
import com.github.retro_game.retro_game.battleengine.Combatant;
import com.github.retro_game.retro_game.entity.BattleResult;
import com.github.retro_game.retro_game.entity.CombatReport;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.service.CombatReportService;

import java.util.Date;
import java.util.List;

public interface CombatReportServiceInternal extends CombatReportService {
  CombatReport create(Date at, List<Combatant> attackers, List<Combatant> defenders, BattleOutcome battleOutcome,
                      BattleResult result, Resources attackersLoss, Resources defendersLoss, Resources plunder,
                      long debrisMetal, long debrisCrystal, double moonChance, boolean moonGiven, int seed,
                      long executionTime);
}
