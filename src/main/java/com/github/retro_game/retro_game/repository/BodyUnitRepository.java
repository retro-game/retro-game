package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.BodyUnit;
import com.github.retro_game.retro_game.entity.BodyUnitKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyUnitRepository extends JpaRepository<BodyUnit, BodyUnitKey> {
}
