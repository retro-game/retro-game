package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.OtherReport;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface OtherReportRepository extends JpaRepository<OtherReport, Long> {
  int countByUserAndDeletedIsFalseAndAtAfter(User user, Date at);

  List<OtherReport> findAllByUserAndDeletedIsFalse(User user, Pageable pageable);

  @Transactional
  @Modifying
  @Query("update OtherReport set deleted = true where user.id = ?1")
  void markAllAsDeletedByUserId(long userId);
}
