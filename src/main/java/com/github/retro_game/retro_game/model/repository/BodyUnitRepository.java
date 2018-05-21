package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.BodyUnit;
import com.github.retro_game.retro_game.model.entity.BodyUnitKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyUnitRepository extends JpaRepository<BodyUnit, BodyUnitKey> {
}
