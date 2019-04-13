package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.BroadcastMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BroadcastMessageRepository extends CrudRepository<BroadcastMessage, Long> {
  List<BroadcastMessage> getAllByOrderByAtDesc(Pageable pageable);
}
