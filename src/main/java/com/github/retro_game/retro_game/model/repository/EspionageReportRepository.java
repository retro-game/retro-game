package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.EspionageReport;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface EspionageReportRepository extends JpaRepository<EspionageReport, Long>,
    EspionageReportRepositoryCustom {
  int countByUserAndDeletedIsFalseAndAtAfter(User user, Date at);
}
