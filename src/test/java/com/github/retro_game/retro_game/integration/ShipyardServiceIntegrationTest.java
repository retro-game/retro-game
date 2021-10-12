package com.github.retro_game.retro_game.integration;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.entity.*;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.service.BodyService;
import com.github.retro_game.retro_game.service.ShipyardService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.service.exception.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

public class ShipyardServiceIntegrationTest extends IntegrationTest {
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private BodyService bodyService;
  @Autowired
  private ShipyardService shipyardService;
  @Autowired
  private UserService userService;

  private static Resources makeResources(double metal, double crystal, double deuterium) {
    var res = new Resources();
    res.setMetal(metal);
    res.setCrystal(crystal);
    res.setDeuterium(deuterium);
    return res;
  }

  private long createUser() {
    return userService.create("test@test", "test", "test");
  }

  private Body createBody(long userId) {
    var user = userRepository.findById(userId).orElseThrow();
    var coordinates = new Coordinates();
    coordinates.setGalaxy(1);
    coordinates.setSystem(1);
    coordinates.setPosition(1);
    coordinates.setKind(CoordinatesKind.PLANET);
    var now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    return bodyService.createColony(user, coordinates, now);
  }

  @Test(expected = RequirementsNotMetException.class)
  @Transactional
  public void build_buildingsRequirementsNotMet_noBuildings() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    shipyardService.build(body.getId(), UnitKindDto.ROCKET_LAUNCHER, 1);
  }

  @Test(expected = RequirementsNotMetException.class)
  @Transactional
  public void build_buildingsRequirementsNotMet_buildingsTooLow() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 1); // too low
    shipyardService.build(body.getId(), UnitKindDto.ANTI_BALLISTIC_MISSILE, 1);
  }

  @Test(expected = RequirementsNotMetException.class)
  @Transactional
  public void build_technologiesRequirementsNotMet_noTechnologies() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 2);
    shipyardService.build(body.getId(), UnitKindDto.SMALL_CARGO, 1);
  }

  @Test(expected = RequirementsNotMetException.class)
  @Transactional
  public void build_technologiesRequirementsNotMet_technologiesTooLow() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 2);
    body.getUser().setTechnologyLevel(TechnologyKind.COMBUSTION_DRIVE, 1); // too low
    shipyardService.build(body.getId(), UnitKindDto.SMALL_CARGO, 1);
  }

  @Test(expected = NotEnoughResourcesException.class)
  @Transactional
  public void build_notEnoughResources_oneUnit() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1000.0, 0, 0));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    shipyardService.build(body.getId(), UnitKindDto.ROCKET_LAUNCHER, 1);
  }

  @Test(expected = NotEnoughResourcesException.class)
  @Transactional
  public void build_notEnoughResources_manyUnits() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(19_000.0, 0, 0));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    shipyardService.build(body.getId(), UnitKindDto.ROCKET_LAUNCHER, 10);
  }

  @Test(expected = TooManyShieldDomesException.class)
  @Transactional
  public void build_tooManyShieldDomes_smallShieldDome() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.getUser().setTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY, 2);
    shipyardService.build(body.getId(), UnitKindDto.SMALL_SHIELD_DOME, 2);
  }

  @Test(expected = TooManyShieldDomesException.class)
  @Transactional
  public void build_tooManyShieldDomes_largeShieldDome() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 6);
    body.getUser().setTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY, 6);
    shipyardService.build(body.getId(), UnitKindDto.LARGE_SHIELD_DOME, 2);
  }

  @Test(expected = ShieldDomeAlreadyBuiltException.class)
  @Transactional
  public void build_shieldDomeAlreadyBuilt_smallShieldDome() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.getUser().setTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY, 2);
    body.setUnitsCount(UnitKind.SMALL_SHIELD_DOME, 1);
    shipyardService.build(body.getId(), UnitKindDto.SMALL_SHIELD_DOME, 1);
  }

  @Test(expected = ShieldDomeAlreadyBuiltException.class)
  @Transactional
  public void build_shieldDomeAlreadyBuilt_largeShieldDome() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 6);
    body.getUser().setTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY, 6);
    body.setUnitsCount(UnitKind.LARGE_SHIELD_DOME, 1);
    shipyardService.build(body.getId(), UnitKindDto.LARGE_SHIELD_DOME, 1);
  }

  @Test(expected = ShieldDomeAlreadyInQueueException.class)
  @Transactional
  public void build_shieldDomeAlreadyInQueue_smallShieldDome() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.getUser().setTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY, 2);
    Assertions.assertThatCode(
        () -> shipyardService.build(body.getId(), UnitKindDto.SMALL_SHIELD_DOME, 1)
    ).doesNotThrowAnyException();
    shipyardService.build(body.getId(), UnitKindDto.SMALL_SHIELD_DOME, 1);
  }

  @Test(expected = ShieldDomeAlreadyInQueueException.class)
  @Transactional
  public void build_shieldDomeAlreadyInQueue_largeShieldDome() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 6);
    body.getUser().setTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY, 6);
    Assertions.assertThatCode(
        () -> shipyardService.build(body.getId(), UnitKindDto.LARGE_SHIELD_DOME, 1)
    ).doesNotThrowAnyException();
    shipyardService.build(body.getId(), UnitKindDto.LARGE_SHIELD_DOME, 1);
  }

  @Test(expected = NotEnoughCapacityException.class)
  @Transactional
  public void build_notEnoughCapacity_planetFullABM_ABM() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 4); // 40 capacity
    body.getUser().setTechnologyLevel(TechnologyKind.IMPULSE_DRIVE, 1);
    body.setUnitsCount(UnitKind.ANTI_BALLISTIC_MISSILE, 40);
    shipyardService.build(body.getId(), UnitKindDto.ANTI_BALLISTIC_MISSILE, 1);
  }

  @Test(expected = NotEnoughCapacityException.class)
  @Transactional
  public void build_notEnoughCapacity_planetFullABM_IPM() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 4); // 40 capacity
    body.getUser().setTechnologyLevel(TechnologyKind.IMPULSE_DRIVE, 1);
    body.setUnitsCount(UnitKind.ANTI_BALLISTIC_MISSILE, 39);
    shipyardService.build(body.getId(), UnitKindDto.INTERPLANETARY_MISSILE, 1);
  }

  @Test(expected = NotEnoughCapacityException.class)
  @Transactional
  public void build_notEnoughCapacity_planetFullIPM_ABM() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 4); // 40 capacity
    body.getUser().setTechnologyLevel(TechnologyKind.IMPULSE_DRIVE, 1);
    body.setUnitsCount(UnitKind.INTERPLANETARY_MISSILE, 20);
    shipyardService.build(body.getId(), UnitKindDto.ANTI_BALLISTIC_MISSILE, 1);
  }

  @Test(expected = NotEnoughCapacityException.class)
  @Transactional
  public void build_notEnoughCapacity_planetFullIPM_IPM() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 4); // 40 capacity
    body.getUser().setTechnologyLevel(TechnologyKind.IMPULSE_DRIVE, 1);
    body.setUnitsCount(UnitKind.INTERPLANETARY_MISSILE, 20);
    shipyardService.build(body.getId(), UnitKindDto.INTERPLANETARY_MISSILE, 1);
  }

  @Test(expected = NotEnoughCapacityException.class)
  @Transactional
  public void build_notEnoughCapacity_planetFullMixed_ABM() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 4); // 40 capacity
    body.getUser().setTechnologyLevel(TechnologyKind.IMPULSE_DRIVE, 1);
    body.setUnitsCount(UnitKind.ANTI_BALLISTIC_MISSILE, 20);
    body.setUnitsCount(UnitKind.INTERPLANETARY_MISSILE, 10);
    shipyardService.build(body.getId(), UnitKindDto.ANTI_BALLISTIC_MISSILE, 1);
  }

  @Test(expected = NotEnoughCapacityException.class)
  @Transactional
  public void build_notEnoughCapacity_planetFullMixed_IPM() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 4); // 40 capacity
    body.getUser().setTechnologyLevel(TechnologyKind.IMPULSE_DRIVE, 1);
    body.setUnitsCount(UnitKind.ANTI_BALLISTIC_MISSILE, 19);
    body.setUnitsCount(UnitKind.INTERPLANETARY_MISSILE, 10);
    shipyardService.build(body.getId(), UnitKindDto.INTERPLANETARY_MISSILE, 1);
  }

  @Test(expected = NotEnoughCapacityException.class)
  @Transactional
  public void build_notEnoughCapacity_planetAndQueueFull_ABM() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 4); // 40 capacity
    body.getUser().setTechnologyLevel(TechnologyKind.IMPULSE_DRIVE, 1);
    body.setUnitsCount(UnitKind.ANTI_BALLISTIC_MISSILE, 10);
    body.setUnitsCount(UnitKind.INTERPLANETARY_MISSILE, 5);
    Assertions.assertThatCode(
        () -> shipyardService.build(body.getId(), UnitKindDto.INTERPLANETARY_MISSILE, 5)
    ).doesNotThrowAnyException();
    Assertions.assertThatCode(
        () -> shipyardService.build(body.getId(), UnitKindDto.ANTI_BALLISTIC_MISSILE, 10)
    ).doesNotThrowAnyException();
    shipyardService.build(body.getId(), UnitKindDto.ANTI_BALLISTIC_MISSILE, 1);
  }

  @Test(expected = NotEnoughCapacityException.class)
  @Transactional
  public void build_notEnoughCapacity_planetAndQueueFull_IPM() {
    var userId = createUser();
    var body = createBody(userId);
    body.setResources(makeResources(1e9, 1e9, 1e9));
    body.setBuildingLevel(BuildingKind.SHIPYARD, 1);
    body.setBuildingLevel(BuildingKind.MISSILE_SILO, 4); // 40 capacity
    body.getUser().setTechnologyLevel(TechnologyKind.IMPULSE_DRIVE, 1);
    body.setUnitsCount(UnitKind.ANTI_BALLISTIC_MISSILE, 10);
    body.setUnitsCount(UnitKind.INTERPLANETARY_MISSILE, 5);
    Assertions.assertThatCode(
        () -> shipyardService.build(body.getId(), UnitKindDto.INTERPLANETARY_MISSILE, 5)
    ).doesNotThrowAnyException();
    Assertions.assertThatCode(
        () -> shipyardService.build(body.getId(), UnitKindDto.ANTI_BALLISTIC_MISSILE, 9)
    ).doesNotThrowAnyException();
    shipyardService.build(body.getId(), UnitKindDto.INTERPLANETARY_MISSILE, 1);
  }
}
