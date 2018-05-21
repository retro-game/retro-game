package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Event;
import com.github.retro_game.retro_game.model.entity.EventKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
  List<Event> findByKindAndParamIn(EventKind kind, Collection<Long> params);

  Optional<Event> findFirstByKindAndParam(EventKind kind, long param);

  Optional<Event> findFirstByOrderByAtAscIdAsc();
}
