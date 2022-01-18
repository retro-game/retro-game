package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.CombatReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CombatReportRepository extends JpaRepository<CombatReport, UUID> {
}
