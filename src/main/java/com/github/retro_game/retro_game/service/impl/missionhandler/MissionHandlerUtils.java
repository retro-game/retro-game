package com.github.retro_game.retro_game.service.impl.missionhandler;

import com.github.retro_game.retro_game.entity.Event;
import com.github.retro_game.retro_game.entity.EventKind;
import com.github.retro_game.retro_game.entity.Flight;
import com.github.retro_game.retro_game.service.impl.EventScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MissionHandlerUtils {
  private EventScheduler eventScheduler;

  @Autowired
  public void setEventScheduler(EventScheduler eventScheduler) {
    this.eventScheduler = eventScheduler;
  }

  public void scheduleReturn(Flight flight) {
    var event = new Event(0, flight.getReturnAt(), EventKind.FLIGHT, flight.getId());
    eventScheduler.schedule(event);
  }
}
