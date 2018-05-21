package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.CombatReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CombatReportRepository extends JpaRepository<CombatReport, Long> {
}
