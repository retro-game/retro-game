package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.Technology;
import com.github.retro_game.retro_game.entity.TechnologyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyRepository extends JpaRepository<Technology, TechnologyKey> {
}
