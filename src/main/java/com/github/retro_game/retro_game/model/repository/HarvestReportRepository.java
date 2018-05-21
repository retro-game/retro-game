package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.HarvestReport;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface HarvestReportRepository extends JpaRepository<HarvestReport, Long>, HarvestReportRepositoryCustom {
  int countByUserAndDeletedIsFalseAndAtAfter(User user, Date at);
}
