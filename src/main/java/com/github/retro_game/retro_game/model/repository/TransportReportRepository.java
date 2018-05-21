package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.TransportReport;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface TransportReportRepository extends JpaRepository<TransportReport, Long>,
    TransportReportRepositoryCustom {
  int countByUserAndDeletedIsFalseAndAtAfter(User user, Date at);
}
