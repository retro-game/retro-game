package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.BroadcastMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface BroadcastMessageRepository extends CrudRepository<BroadcastMessage, Long> {
  long countByAtAfter(Date at);

  List<BroadcastMessage> getAllByOrderByAtDesc(Pageable pageable);
}
