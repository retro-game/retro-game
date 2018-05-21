package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.TechnologyQueueEntry;
import com.github.retro_game.retro_game.model.entity.TechnologyQueueEntryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyQueueEntryRepository extends JpaRepository<TechnologyQueueEntry, TechnologyQueueEntryKey> {
}
