package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.FlightUnit;
import com.github.retro_game.retro_game.entity.FlightUnitKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightUnitRepository extends JpaRepository<FlightUnit, FlightUnitKey> {
}
