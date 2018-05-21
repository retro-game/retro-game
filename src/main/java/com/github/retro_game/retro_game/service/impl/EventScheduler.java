package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.Event;
import com.github.retro_game.retro_game.model.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
class EventScheduler implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(EventScheduler.class);
  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();
  private final TaskExecutor eventSchedulerThread;
  private final EventRepository eventRepository;
  private BuildingsServiceInternal buildingsServiceInternal;
  private FlightServiceInternal flightServiceInternal;
  private ShipyardServiceInternal shipyardServiceInternal;
  private TechnologyServiceInternal technologyServiceInternal;

  public EventScheduler(TaskExecutor eventSchedulerThread, EventRepository eventRepository) {
    this.eventSchedulerThread = eventSchedulerThread;
    this.eventRepository = eventRepository;
  }

  @Autowired
  public void setBuildingsServiceInternal(BuildingsServiceInternal buildingsServiceInternal) {
    this.buildingsServiceInternal = buildingsServiceInternal;
  }

  @Autowired
  public void setFlightServiceInternal(FlightServiceInternal flightServiceInternal) {
    this.flightServiceInternal = flightServiceInternal;
  }

  @Autowired
  public void setShipyardServiceInternal(ShipyardServiceInternal shipyardServiceInternal) {
    this.shipyardServiceInternal = shipyardServiceInternal;
  }

  @Autowired
  public void setTechnologyServiceInternal(TechnologyServiceInternal technologyServiceInternal) {
    this.technologyServiceInternal = technologyServiceInternal;
  }

  @PostConstruct
  private void start() {
    this.eventSchedulerThread.execute(this);
  }

  void schedule(Event event) {
    eventRepository.save(event);
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        try {
          lock.lock();
          condition.signal();
        } finally {
          lock.unlock();
        }
      }
    });
  }

  private Event getNext() throws InterruptedException {
    lock.lock();
    try {
      Optional<Event> event;
      while (!(event = eventRepository.findFirstByOrderByAtAscIdAsc()).isPresent() ||
          event.get().getAt().after(new Date())) {
        if (event.isPresent()) {
          condition.awaitUntil(event.get().getAt());
        } else {
          condition.await();
        }
      }
      return event.get();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void run() {
    int numRetries = 0;
    while (numRetries < 16) {
      Event event;
      try {
        event = getNext();
      } catch (InterruptedException e) {
        logger.info("Interrupted");
        continue;
      }
      try {
        switch (event.getKind()) {
          case BUILDING_QUEUE:
            buildingsServiceInternal.handle(event);
            break;
          case SHIPYARD_QUEUE:
            shipyardServiceInternal.handle(event);
            break;
          case TECHNOLOGY_QUEUE:
            technologyServiceInternal.handle(event);
            break;
          case FLIGHT:
            flightServiceInternal.handle(event);
            break;
          default:
            logger.error("Wrong event kind");
            return;
        }
        numRetries = 0;
      } catch (TransactionException e) {
        logger.warn("Transaction failed: msg={}", e.getMessage());
        numRetries++;
      }
    }
    logger.error("A transaction failed too many times");
  }
}
