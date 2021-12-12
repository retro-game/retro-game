package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.FlightEventDto;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.model.ItemUtils;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.FlightEventsService;
import com.github.retro_game.retro_game.service.PhalanxService;
import com.github.retro_game.retro_game.service.exception.BodyDoesNotExistException;
import com.github.retro_game.retro_game.service.exception.NotEnoughDeuteriumException;
import com.github.retro_game.retro_game.service.exception.TargetOutOfRangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
class PhalanxServiceImpl implements PhalanxService {
  private final Logger logger = LoggerFactory.getLogger(PhalanxServiceImpl.class);
  private final int phalanxScanCost;
  private final BodyRepository bodyRepository;
  private BodyServiceInternal bodyServiceInternal;
  private FlightEventsService flightEventsService;

  public PhalanxServiceImpl(@Value("${retro-game.phalanx-scan-cost}") int phalanxScanCost,
                            BodyRepository bodyRepository) {
    this.phalanxScanCost = phalanxScanCost;
    this.bodyRepository = bodyRepository;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Autowired
  public void setFlightEventsService(FlightEventsService flightEventsService) {
    this.flightEventsService = flightEventsService;
  }

  @PostConstruct
  private void checkProperties() {
    Assert.isTrue(phalanxScanCost >= 0, "retro-game.phalanx-scan-cost must be at least 0");
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public List<FlightEventDto> scan(long bodyId, int galaxy, int system, int position) {
    Body body = bodyServiceInternal.getUpdated(bodyId);

    Coordinates coordinates = new Coordinates(galaxy, system, position, CoordinatesKind.PLANET);

    Optional<Body> targetOptional = bodyRepository.findByCoordinates(coordinates);
    if (!targetOptional.isPresent()) {
      logger.warn("Phalanx scanning failed, target doesn't exist: userId={} bodyId={} targetCoordinates={}-{}-{}-P",
          CustomUser.getCurrentUserId(), body.getId(), galaxy, system, position);
      throw new BodyDoesNotExistException();
    }
    Body target = targetOptional.get();

    var phalanxLevel = body.getBuildingLevel(BuildingKind.SENSOR_PHALANX);
    if (!ItemUtils.isWithinPhalanxRange(body.getCoordinates(), target.getCoordinates(), phalanxLevel)) {
      logger.warn("Phalanx scanning failed, system out of range: userId={} bodyId={} targetCoordinates={}-{}-{}-P",
          CustomUser.getCurrentUserId(), body.getId(), galaxy, system, position);
      throw new TargetOutOfRangeException();
    }

    Resources resources = body.getResources();
    if (resources.getDeuterium() < phalanxScanCost) {
      logger.info("Phalanx scanning failed, not enough deuterium: userId={} bodyId={} targetCoordinates={}-{}-{}-P",
          CustomUser.getCurrentUserId(), body.getId(), galaxy, system, position);
      throw new NotEnoughDeuteriumException();
    }
    resources.setDeuterium(resources.getDeuterium() - phalanxScanCost);

    logger.info("Phalanx scanning: userId={} bodyId={} targetCoordinates={}-{}-{}-P", CustomUser.getCurrentUserId(),
        body.getId(), galaxy, system, position);
    return flightEventsService.getPhalanxFlightEvents(galaxy, system, position);
  }
}
