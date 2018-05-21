package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Building;
import com.github.retro_game.retro_game.model.entity.BuildingKey;
import com.github.retro_game.retro_game.model.entity.BuildingKind;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, BuildingKey> {
  @Query("SELECT building FROM Building building JOIN Body body ON body = building.key.body WHERE body.user = :user AND building.key.kind = :kind ORDER BY building.level DESC")
  List<Building> findByUserAndKindOrderByLevelDesc(@Param("user") User user, @Param("kind") BuildingKind kind);
}
