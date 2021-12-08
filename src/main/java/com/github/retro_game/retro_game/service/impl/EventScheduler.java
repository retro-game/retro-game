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
  private static final int MAX_WAIT_TIME_IN_MS = 3000;
  private static final Logger logger = LoggerFactory.getLogger(EventScheduler.class);
  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();
  private final TaskExecutor eventSchedulerThread;
  private final EventRepository eventRepository;
  private BuildingsServiceInternal buildingsServiceInternal;
  private FlightServiceInternal flightServiceInternal;
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
      try {
        var event = getNext();
        switch (event.getKind()) {
          case BUILDING_QUEUE -> buildingsServiceInternal.handle(event);
          case TECHNOLOGY_QUEUE -> technologyServiceInternal.handle(event);
          case FLIGHT -> flightServiceInternal.handle(event);
          case SHIPYARD_QUEUE -> {
            logger.error("Shipyard queue event, this shouldn't happen");
            eventRepository.delete(event);
          }
          default -> {
            logger.error("Wrong event kind");
            return;
          }
        }
        waitTime = 0;
      } catch (Exception e) {
        if (e instanceof InterruptedException) {
          logger.info("Interrupted");
          continue;
        } else if (e instanceof DataAccessException) {
          waitTime = Math.min(MAX_WAIT_TIME_IN_MS, waitTime + WAIT_TIME_STEP_IN_MS);
          logger.warn("Scheduler transaction failed, retrying in {}ms: msg={}", waitTime, e.getMessage());
        } else {
          waitTime = MAX_WAIT_TIME_IN_MS;
          logger.error("Scheduler unexpected error, retrying in {}ms: msg={}", waitTime, e.getMessage());
        }

        try {
          Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
          logger.warn("Waiting interrupted");
        }
      }
    }
  }
}
