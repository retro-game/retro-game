package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.TechnologyQueueEntry;
import com.github.retro_game.retro_game.entity.TechnologyQueueEntryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyQueueEntryRepository extends JpaRepository<TechnologyQueueEntry, TechnologyQueueEntryKey> {
}
