package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.battleengine.BattleOutcome;
import com.github.retro_game.retro_game.battleengine.Combatant;
import com.github.retro_game.retro_game.dto.CombatReportDto;
import com.github.retro_game.retro_game.dto.MoonCreationResultDto;
import com.github.retro_game.retro_game.dto.MoonDestructionResultDto;
import com.github.retro_game.retro_game.entity.BattleResult;
import com.github.retro_game.retro_game.entity.CombatReport;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.repository.CombatReportRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.exception.ReportDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CombatReportServiceImpl implements CombatReportServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(CombatReportServiceImpl.class);
  private final CombatReportRepository combatReportRepository;

  public CombatReportServiceImpl(CombatReportRepository combatReportRepository) {
    this.combatReportRepository = combatReportRepository;
  }

  @Override
  public CombatReport create(Date at, List<Combatant> attackers, List<Combatant> defenders, BattleOutcome battleOutcome,
                             BattleResult result, Resources attackersLoss, Resources defendersLoss, Resources plunder,
                             Resources debris, MoonCreationResultDto moonCreationResult,
                             @Nullable MoonDestructionResultDto moonDestructionResult, int seed, long executionTime) {
    var id = UUID.randomUUID();

    var attackerIds = attackers.stream().mapToLong(Combatant::userId).distinct().toArray();
    var defenderIds = defenders.stream().mapToLong(Combatant::userId).distinct().toArray();

    var totalAttackersLoss =
        (long) (attackersLoss.getMetal() + attackersLoss.getCrystal() + attackersLoss.getDeuterium());
    var totalDefendersLoss =
        (long) (defendersLoss.getMetal() + defendersLoss.getCrystal() + defendersLoss.getDeuterium());

    Double moonDestructionChance = null, deathStarsDestructionChance = null;
    Boolean moonDestroyed = null, deathStarsDestroyed = null;
    if (moonDestructionResult != null) {
      moonDestructionChance = moonDestructionResult.moonDestructionChance();
      moonDestroyed = moonDestructionResult.moonDestroyed();
      deathStarsDestructionChance = moonDestructionResult.deathStarsDestructionChance();
      deathStarsDestroyed = moonDestructionResult.deathStarsDestroyed();
    }

    var data = CombatReportSerialization.serialize(attackers, defenders, battleOutcome);

    var report =
        new CombatReport(id, at, attackerIds, defenderIds, result, totalAttackersLoss, totalDefendersLoss, plunder,
            (long) debris.getMetal(), (long) debris.getCrystal(), moonCreationResult.chance(),
            moonCreationResult.created(), moonDestructionChance, moonDestroyed, deathStarsDestructionChance,
            deathStarsDestroyed, seed, executionTime, data);
    combatReportRepository.save(report);
    return report;
  }

  @Override
  public CombatReportDto get(UUID id) {
    var userId = 0L;
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof CustomUser customUser) {
      userId = customUser.getUserId();
    }

    var reportOpt = combatReportRepository.findById(id);
    if (reportOpt.isEmpty()) {
      logger.warn("Getting combat report failed, report doesn't exist: userId={} reportId={}", userId, id);
      throw new ReportDoesNotExistException();
    }
    var report = reportOpt.get();

    logger.info("Getting combat report: userId={} reportId={}", userId, id);

    MoonDestructionResultDto moonDestructionResult = null;
    if (report.getMoonDestructionChance() != null) {
      assert report.getMoonDestroyed() != null && report.getDeathStarsDestructionChance() != null &&
          report.getDeathStarsDestroyed() != null;
      moonDestructionResult = new MoonDestructionResultDto(report.getMoonDestructionChance(), report.getMoonDestroyed(),
          report.getDeathStarsDestructionChance(), report.getDeathStarsDestroyed());
    }

    var data = CombatReportSerialization.deserialize(report.getData());

    return new CombatReportDto(report.getAt(), data.attackers(), data.defenders(), data.rounds(),
        Converter.convert(report.getResult()), report.getAttackersLoss(), report.getDefendersLoss(),
        Converter.convert(report.getPlunder()), report.getDebrisMetal(), report.getDebrisCrystal(),
        report.getMoonChance(), report.isMoonGiven(), moonDestructionResult, report.getSeed(),
        report.getExecutionTime());
  }
}
