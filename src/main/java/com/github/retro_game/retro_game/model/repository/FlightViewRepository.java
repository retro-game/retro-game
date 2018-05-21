package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Coordinates;
import com.github.retro_game.retro_game.model.entity.FlightView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface FlightViewRepository extends JpaRepository<FlightView, Long> {
  List<FlightView> findAllByStartUserId(long userId);

  List<FlightView> findAllByStartUserIdOrTargetUserIdOrPartyIdIn(long startUserId, long targetUserId,
                                                                 Set<Long> partiesIds);

  List<FlightView> findAllByStartCoordinatesOrTargetCoordinates(Coordinates start, Coordinates target);
}
