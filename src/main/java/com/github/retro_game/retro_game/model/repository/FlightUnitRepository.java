package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.FlightUnit;
import com.github.retro_game.retro_game.model.entity.FlightUnitKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightUnitRepository extends JpaRepository<FlightUnit, FlightUnitKey> {
}
