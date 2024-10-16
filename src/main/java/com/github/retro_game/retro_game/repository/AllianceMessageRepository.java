package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.AllianceMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface AllianceMessageRepository extends JpaRepository<AllianceMessage, Long> {
  @Query("select count(message.id) from AllianceMessage message join AllianceMember member_ " +
      "on member_.key.alliance.id = message.allianceId where member_.key.user.id = ?1 and message.at > ?2")
  long countByMemberIdAndAtAfter(long memberId, Date at);

  List<AllianceMessage> getAllByAllianceIdOrderByAtDesc(long allianceId, Pageable pageable);
}
