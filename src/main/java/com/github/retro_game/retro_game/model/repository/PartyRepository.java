package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, Long> {
}
