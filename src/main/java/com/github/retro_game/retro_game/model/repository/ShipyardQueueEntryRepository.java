package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.ShipyardQueueEntry;
import com.github.retro_game.retro_game.model.entity.ShipyardQueueEntryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipyardQueueEntryRepository extends JpaRepository<ShipyardQueueEntry, ShipyardQueueEntryKey> {
}
