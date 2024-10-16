package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.Alliance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllianceRepository extends JpaRepository<Alliance, Long> {
  boolean existsByTagIgnoreCase(String tag);

  boolean existsByNameIgnoreCase(String name);
}
