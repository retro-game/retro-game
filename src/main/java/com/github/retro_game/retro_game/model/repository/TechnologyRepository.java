package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Technology;
import com.github.retro_game.retro_game.model.entity.TechnologyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyRepository extends JpaRepository<Technology, TechnologyKey> {
}
