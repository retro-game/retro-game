package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {
  List<Party> findByTargetBody(Body body);
}
