package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.PhalanxFlightEventDto;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.repository.BodyRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.PhalanxService;
import com.github.retro_game.retro_game.service.exception.BodyDoesntExistException;
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
  private FlightServiceInternal flightServiceInternal;

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
  public void setFlightServiceInternal(FlightServiceInternal flightServiceInternal) {
    this.flightServiceInternal = flightServiceInternal;
  }

  @PostConstruct
  private void checkProperties() {
    Assert.isTrue(phalanxScanCost >= 0, "retro-game.phalanx-scan-cost must be at least 0");
  }

  @Override
  @Transactional(readOnly = true)
  public boolean systemWithinRange(long bodyId, int galaxy, int system) {
    Body body = bodyRepository.getOne(bodyId);
    return systemWithinRange(body, galaxy, system);
  }

  private boolean systemWithinRange(Body body, int galaxy, int system) {
    if (body.getCoordinates().getGalaxy() != galaxy) {
      return false;
    }

    Building sensorPhalanx = body.getBuildings().get(BuildingKind.SENSOR_PHALANX);
    if (sensorPhalanx == null) {
      return false;
    }

    int diff = Math.abs(body.getCoordinates().getSystem() - system);
    int level = sensorPhalanx.getLevel();
    int range = level * level - 1;
    return Math.abs(Math.min(diff, 500 - diff)) <= range;
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public List<PhalanxFlightEventDto> scan(long bodyId, int galaxy, int system, int position) {
    Body body = bodyServiceInternal.getUpdated(bodyId);

    Coordinates coordinates = new Coordinates(galaxy, system, position, CoordinatesKind.PLANET);

    Optional<Body> targetOptional = bodyRepository.findByCoordinates(coordinates);
    if (!targetOptional.isPresent()) {
      logger.warn("Phalanx scanning failed, target doesn't exist: userId={} bodyId={} targetCoordinates={}-{}-{}-P",
          CustomUser.getCurrentUserId(), body.getId(), galaxy, system, position);
      throw new BodyDoesntExistException();
    }
    Body target = targetOptional.get();

    if (!systemWithinRange(body, target.getCoordinates().getGalaxy(), target.getCoordinates().getSystem())) {
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
    return flightServiceInternal.getPhalanxFlightEvents(galaxy, system, position);
  }
}
