package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Body;
import com.github.retro_game.retro_game.model.entity.Coordinates;
import com.github.retro_game.retro_game.model.entity.CoordinatesKind;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodyRepository extends JpaRepository<Body, Long> {
  long countByUserAndCoordinatesKind(User user, CoordinatesKind kind);

  boolean existsByCoordinates(Coordinates coordinates);

  boolean existsByIdAndUser_Id(long id, long userId);

  Optional<Body> findByCoordinates(Coordinates coordinates);
}
