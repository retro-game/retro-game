package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.ShipyardQueueEntry;
import com.github.retro_game.retro_game.entity.ShipyardQueueEntryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipyardQueueEntryRepository extends JpaRepository<ShipyardQueueEntry, ShipyardQueueEntryKey> {
}
