package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.FreeSystem;
import com.github.retro_game.retro_game.entity.FreeSystemKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreeSystemRepository extends JpaRepository<FreeSystem, FreeSystemKey> {
  FreeSystem findFirstBy();
}
