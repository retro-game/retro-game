package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BodyRepository extends JpaRepository<Body, Long>, BodyRepositoryCustom {
  long countByUserAndCoordinatesKind(User user, CoordinatesKind kind);

  boolean existsByCoordinates(Coordinates coordinates);

  boolean existsByIdAndUser_Id(long id, long userId);

  Optional<Body> findByCoordinates(Coordinates coordinates);

  @Query("select body.id from Body body where body.user.id = ?1 order by body.id")
  List<Long> findIdsByUserIdOrderById(long userId);
}
