package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Alliance;
import com.github.retro_game.retro_game.model.entity.AllianceMember;
import com.github.retro_game.retro_game.model.entity.AllianceMemberKey;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllianceMemberRepository extends JpaRepository<AllianceMember, AllianceMemberKey> {
  // Returns the number of members.
  long countByKey_Alliance(Alliance alliance);

  // Checks whether user has already an alliance.
  boolean existsByKey_User(User user);

  Optional<AllianceMember> findByKey_User(User user);
}
