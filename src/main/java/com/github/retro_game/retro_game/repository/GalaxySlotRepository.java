package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.GalaxySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalaxySlotRepository extends JpaRepository<GalaxySlot, Long> {
  List<GalaxySlot> findAllByGalaxyAndSystem(int galaxy, int system);
}
