package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.FreeSystem;
import com.github.retro_game.retro_game.model.entity.FreeSystemKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreeSystemRepository extends JpaRepository<FreeSystem, FreeSystemKey> {
  FreeSystem findFirstBy();
}
