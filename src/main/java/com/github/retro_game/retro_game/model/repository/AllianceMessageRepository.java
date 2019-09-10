package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.AllianceMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllianceMessageRepository extends JpaRepository<AllianceMessage, Long> {
  List<AllianceMessage> getAllByAllianceIdOrderByAtDesc(long allianceId, Pageable pageable);
}
