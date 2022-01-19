package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.CombatReportSortOrderDto;
import com.github.retro_game.retro_game.dto.HallOfFameEntryDto;
import com.github.retro_game.retro_game.dto.ResourcesDto;
import com.github.retro_game.retro_game.repository.CombatReportRepository;
import com.github.retro_game.retro_game.service.HallOfFameService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class HallOfFameServiceImpl implements HallOfFameService {
  private final int hallOfFameDelay;
  private final int hallOfFameNumEntries;
  private final CombatReportRepository combatReportRepository;

  public HallOfFameServiceImpl(@Value("${retro-game.hall-of-fame-delay}") int hallOfFameDelay,
                               @Value("${retro-game.hall-of-fame-num-entries}") int hallOfFameNumEntries,
                               CombatReportRepository combatReportRepository) {
    this.hallOfFameDelay = hallOfFameDelay;
    this.hallOfFameNumEntries = hallOfFameNumEntries;
    this.combatReportRepository = combatReportRepository;
  }

  @Override
  public ArrayList<HallOfFameEntryDto> get(CombatReportSortOrderDto sortOrder) {
    var delay = hallOfFameDelay + " hours";
    var reports = switch (sortOrder) {
      case LOSS -> combatReportRepository.findTopReportsOrderByLoss(delay, hallOfFameNumEntries);
      case PLUNDER -> combatReportRepository.findTopReportsOrderByPlunder(delay, hallOfFameNumEntries);
      case DEBRIS -> combatReportRepository.findTopReportsOrderByDebris(delay, hallOfFameNumEntries);
    };

    var i = 1;
    var rank = 1;
    var prev = -1L;
    var entries = new ArrayList<HallOfFameEntryDto>(reports.size());
    for (var report : reports) {
      var plunder = Converter.convert(report.getPlunder());
      var debris = new ResourcesDto(report.getDebrisMetal(), report.getDebrisCrystal(), 0.0);

      var value = switch (sortOrder) {
        case LOSS -> report.getAttackersLoss() + report.getDefendersLoss();
        case PLUNDER -> (long) plunder.total();
        case DEBRIS -> (long) debris.total();
      };
      rank = value == prev ? rank : i;
      prev = value;
      i++;

      var attackers = Arrays.stream(report.getAttackers()).boxed().collect(Collectors.toCollection(ArrayList::new));
      var defenders = Arrays.stream(report.getDefenders()).boxed().collect(Collectors.toCollection(ArrayList::new));

      var entry = new HallOfFameEntryDto(rank, attackers, defenders, Converter.convert(report.getResult()),
          report.getAttackersLoss(), report.getDefendersLoss(), plunder, debris, report.getId());
      entries.add(entry);
    }

    return entries;
  }
}
