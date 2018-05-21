package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.SimplifiedCombatReport;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface SimplifiedCombatReportRepository extends JpaRepository<SimplifiedCombatReport, Long>,
    SimplifiedCombatReportRepositoryCustom {
  int countByUserAndDeletedIsFalseAndAtAfter(User user, Date at);
}
