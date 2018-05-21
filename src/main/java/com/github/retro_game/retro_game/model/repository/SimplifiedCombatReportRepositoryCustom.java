package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.SimplifiedCombatReport;
import com.github.retro_game.retro_game.model.entity.SimplifiedCombatReportSortOrder;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface SimplifiedCombatReportRepositoryCustom {
  List<SimplifiedCombatReport> findReports(User user, SimplifiedCombatReportSortOrder order, Sort.Direction direction,
                                           Pageable pageable);
}
