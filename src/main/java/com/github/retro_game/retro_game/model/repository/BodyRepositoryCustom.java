package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Body;
import com.github.retro_game.retro_game.model.entity.CoordinatesKind;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.lang.Nullable;

import java.util.List;

public interface BodyRepositoryCustom {
  List<Body> findByUserForEmpire(User user, @Nullable Integer galaxy, @Nullable Integer system,
                                 @Nullable Integer position, @Nullable CoordinatesKind kind);
}
