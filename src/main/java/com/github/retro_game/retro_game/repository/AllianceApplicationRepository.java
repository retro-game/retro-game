package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.Alliance;
import com.github.retro_game.retro_game.entity.AllianceApplication;
import com.github.retro_game.retro_game.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AllianceApplicationRepository extends JpaRepository<AllianceApplication, Long> {
  long countByAlliance(Alliance alliance);

  boolean existsByUser(User user);

  Optional<AllianceApplication> findByUser(User user);

  List<AllianceApplication> findByAllianceOrderByAtDesc(Alliance alliance);
}
