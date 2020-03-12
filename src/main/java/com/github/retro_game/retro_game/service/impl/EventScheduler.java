package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.Event;
import com.github.retro_game.retro_game.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
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
  private static final int WAIT_TIME_STEP_IN_MS = 10;
  private static final int MAX_WAIT_TIME_IN_MS = 1000;
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
    int waitTime = 0;
    while (true) {
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
        waitTime = 0;
      } catch (DataAccessException e) {
        logger.warn("Transaction failed, waiting {}ms: msg={}", waitTime, e.getMessage());
        try {
          Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
          logger.warn("Waiting interrupted");
        }
        waitTime = Math.min(MAX_WAIT_TIME_IN_MS, waitTime + WAIT_TIME_STEP_IN_MS);
      }
    }
  }
}
