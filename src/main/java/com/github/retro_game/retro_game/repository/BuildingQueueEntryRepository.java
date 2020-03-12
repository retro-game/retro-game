package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.BuildingQueueEntry;
import com.github.retro_game.retro_game.entity.BuildingQueueEntryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingQueueEntryRepository extends JpaRepository<BuildingQueueEntry, BuildingQueueEntryKey> {
}
