package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.DebrisField;
import com.github.retro_game.retro_game.model.entity.DebrisFieldKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebrisFieldRepository extends JpaRepository<DebrisField, DebrisFieldKey> {
  boolean existsByKey_GalaxyAndKey_SystemAndKey_Position(int galaxy, int system, int position);
}
