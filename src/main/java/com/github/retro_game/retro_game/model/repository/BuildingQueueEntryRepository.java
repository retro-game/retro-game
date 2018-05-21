package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.BuildingQueueEntry;
import com.github.retro_game.retro_game.model.entity.BuildingQueueEntryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingQueueEntryRepository extends JpaRepository<BuildingQueueEntry, BuildingQueueEntryKey> {
}
