package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.service.ReportService;

import java.util.Date;
import java.util.List;

interface ReportServiceInternal extends ReportService {
  void createSimplifiedCombatReport(User user, boolean isAttacker, Date at, User enemy, Coordinates coordinates,
                                    BattleResult result, int numRounds, Resources attackersLoss,
                                    Resources defendersLoss, Resources plunder, long debrisMetal,
                                    long debrisCrystal, double moonChance, boolean moonGiven,
                                    CombatReport combatReport);

  void createEspionageReport(Flight flight, List<Flight> holdingFlights, double counterEspionageChance);

  void createHarvestReport(Flight flight, int numRecyclers, long capacity, long harvestedMetal, long harvestedCrystal,
                           long remainingMetal, long remainingCrystal);

  void createTransportReport(Flight flight, User user, User partner, Resources resources);

  void createColonizationReport(Flight flight, Resources resources, Double diameter);

  void createDeploymentReport(Flight flight);

  void createHostileEspionageReport(Flight flight, double counterEspionageChance);

  void createReturnReport(Flight flight);

  void createMissileAttackReport(Flight flight, int totalDestroyed);
}
