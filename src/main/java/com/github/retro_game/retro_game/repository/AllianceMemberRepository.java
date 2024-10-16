package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.Alliance;
import com.github.retro_game.retro_game.entity.AllianceMember;
import com.github.retro_game.retro_game.entity.AllianceMemberKey;
import com.github.retro_game.retro_game.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AllianceMemberRepository extends JpaRepository<AllianceMember, AllianceMemberKey> {
  // Returns the number of members.
  long countByKey_Alliance(Alliance alliance);

  // Checks whether user has already an alliance.
  boolean existsByKey_User(User user);

  Optional<AllianceMember> findByKey_User(User user);

  @Query("select am.key.user.id from AllianceMember am where am.key.alliance = ?1")
  List<Long> findMemberIdsByAlliance(Alliance alliance);
}
