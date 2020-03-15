package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.JumpGateInfoDto;
import com.github.retro_game.retro_game.dto.JumpGateTargetDto;
import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import com.github.retro_game.retro_game.service.JumpGateService;
import com.github.retro_game.retro_game.service.exception.CannotJumpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("jumpGateService")
class JumpGateServiceImpl implements JumpGateService {
  private static final Logger logger = LoggerFactory.getLogger(JumpGateServiceImpl.class);
  private final int jumpGateCoolDownSpeed;
  private BodyServiceInternal bodyServiceInternal;

  public JumpGateServiceImpl(@Value("${retro-game.jump-gate-cool-down-speed}") int jumpGateCoolDownSpeed) {
    this.jumpGateCoolDownSpeed = jumpGateCoolDownSpeed;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Override
  public JumpGateInfoDto getInfo(long bodyId) {
    Body body = bodyServiceInternal.getUpdated(bodyId);
    Date canJumpAt = canJumpAt(body);
    List<JumpGateTargetDto> targets = body.getUser().getBodies().values().stream()
        .filter(b -> b.getId() != bodyId && b.getBuildings().get(BuildingKind.JUMP_GATE) != null)
        .map(b -> new JumpGateTargetDto(b.getId(), b.getName(), Converter.convert(b.getCoordinates()), canJumpAt(b)))
        .collect(Collectors.toList());
    Map<UnitKindDto, Integer> units = new EnumMap<>(UnitKindDto.class);
    for (UnitKind kind : UnitItem.getFleet().keySet()) {
      int count = body.getUnitsCount(kind);
      units.put(Converter.convert(kind), count);
    }
    return new JumpGateInfoDto(canJumpAt, targets, units);
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void jump(long bodyId, long targetId, Map<UnitKindDto, Integer> units) {
    Body body = bodyServiceInternal.getUpdated(bodyId);
    Body target = bodyServiceInternal.getUpdated(targetId);
    if (body.getCoordinates().getKind() != CoordinatesKind.MOON ||
        target.getCoordinates().getKind() != CoordinatesKind.MOON) {
      logger.warn("Jumping failed, body and/or target is not a moon: bodyId={} targetId={}", bodyId, targetId);
      throw new CannotJumpException();
    }

    Date bodyCanJumpAt = canJumpAt(body);
    Date targetCanJumpAt = canJumpAt(target);
    Date now = body.getUpdatedAt();
    if (bodyCanJumpAt == null || targetCanJumpAt == null || now.before(bodyCanJumpAt) || now.before(targetCanJumpAt)) {
      logger.info("Jumping failed, no jump gate or time constraints not satisfied: bodyId={} targetId={}", bodyId,
          targetId);
      throw new CannotJumpException();
    }
    body.setLastJumpAt(now);
    target.setLastJumpAt(now);

    for (Map.Entry<UnitKindDto, Integer> entry : units.entrySet()) {
      UnitKind kind = Converter.convert(entry.getKey());
      if (!UnitItem.getFleet().containsKey(kind)) {
        logger.warn("Jumping failed, trying to jump with a non-fleet unit: bodyId={} targetId={}", bodyId, targetId);
        throw new CannotJumpException();
      }

      if (entry.getValue() == null || entry.getValue() <= 0) {
        continue;
      }

      var count = body.getUnitsCount(kind);
      if (count == 0) {
        continue;
      }
      count = Math.min(entry.getValue(), count);
      assert count >= 1;

      int n = body.getUnitsCount(kind) - count;
      body.setUnitsCount(kind, n);

      var targetCount = target.getUnitsCount(kind) + count;
      target.setUnitsCount(kind, targetCount);
    }

    // FIXME: Log units too.
    logger.info("Jumping: bodyId={} targetId={}", bodyId, targetId);
  }

  private Date canJumpAt(Body body) {
    Date lastJumpAt = body.getLastJumpAt();
    if (lastJumpAt == null) {
      return null;
    }

    int level = body.getBuildingLevel(BuildingKind.JUMP_GATE);
    assert level >= 0;
    if (level == 0) {
      return null;
    }

    int coolDownTime = (7200 >> level) / jumpGateCoolDownSpeed;
    return Date.from(Instant.ofEpochSecond(lastJumpAt.toInstant().getEpochSecond() + coolDownTime));
  }
}
