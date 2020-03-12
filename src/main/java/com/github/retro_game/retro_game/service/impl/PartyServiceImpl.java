package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.Flight;
import com.github.retro_game.retro_game.entity.Mission;
import com.github.retro_game.retro_game.entity.Party;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.FlightRepository;
import com.github.retro_game.retro_game.repository.PartyRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.PartyService;
import com.github.retro_game.retro_game.service.dto.PartyDto;
import com.github.retro_game.retro_game.service.dto.PartyMemberDto;
import com.github.retro_game.retro_game.service.dto.PartyTargetDto;
import com.github.retro_game.retro_game.service.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class PartyServiceImpl implements PartyService {
  private static final Logger logger = LoggerFactory.getLogger(PartyServiceImpl.class);
  private final FlightRepository flightRepository;
  private final PartyRepository partyRepository;
  private final UserRepository userRepository;

  public PartyServiceImpl(FlightRepository flightRepository, PartyRepository partyRepository,
                          UserRepository userRepository) {
    this.flightRepository = flightRepository;
    this.partyRepository = partyRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public PartyDto get(long bodyId, long partyId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<Party> partyOptional = partyRepository.findById(partyId);
    if (!partyOptional.isPresent()) {
      logger.warn("Getting party failed, party doesn't exist: userId={} partyId={}", userId, partyId);
      throw new PartyDoesntExistException();
    }
    Party party = partyOptional.get();

    if (party.getTargetUser().getId() != userId && party.getUsers().stream().noneMatch(u -> u.getId() == userId)) {
      logger.warn("Getting party failed, unauthorized access: userId={} partyId={}", userId, partyId);
      throw new UnauthorizedPartyAccessException();
    }

    List<PartyMemberDto> members = party.getUsers().stream()
        .map(u -> new PartyMemberDto(u.getId(), u.getName()))
        .collect(Collectors.toList());
    boolean canInvite = party.getOwner().getId() == userId;
    return new PartyDto(partyId, party.getTargetUser().getId(), party.getTargetUser().getName(),
        party.getTargetBody().getName(), Converter.convert(party.getTargetCoordinates()), members, canInvite);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartyTargetDto> getPartiesTargets(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return user.getParties().stream()
        .map(party -> new PartyTargetDto(party.getId(), party.getTargetBody().getName(),
            Converter.convert(party.getTargetCoordinates())))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public long create(long bodyId, long flightId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<Flight> flightOptional = flightRepository.findById(flightId);
    if (!flightOptional.isPresent()) {
      logger.warn("Creating party failed, flight doesn't exist: userId={} flightId={}", userId, flightId);
      throw new FlightDoesntExistException();
    }
    Flight flight = flightOptional.get();

    User user = flight.getStartUser();
    if (user.getId() != userId) {
      logger.warn("Creating party failed, unauthorized flight access: userId={} flightId={} flightOwnerId={}",
          userId, flightId, user.getId());
      throw new UnauthorizedFlightAccessException();
    }

    if (flight.getParty() != null) {
      logger.warn("Creating party failed, party exists: userId={} flightId={} partyId={}", userId, flightId,
          flight.getParty().getId());
      throw new PartyExistsException();
    }

    if (flight.getArrivalAt() == null) {
      logger.warn("Creating party failed, flight already recalled: userId={} flightId={} partyId={}", userId, flightId,
          flight.getParty().getId());
      throw new FlightAlreadyRecalledException();
    }

    if (flight.getMission() != Mission.ATTACK && flight.getMission() != Mission.DESTROY) {
      logger.warn("Creating party failed, wrong mission: userId={} flightId={} partyId={} mission={}", userId, flightId,
          flight.getParty().getId(), flight.getMission());
      throw new WrongMissionException();
    }

    assert flight.getTargetUser() != null;
    assert flight.getTargetBody() != null;

    Party party = new Party();
    party.setOwner(user);
    party.setTargetUser(flight.getTargetUser());
    party.setTargetBody(flight.getTargetBody());
    party.setTargetCoordinates(flight.getTargetCoordinates());
    party.setMission(flight.getMission());
    party.setUsers(Collections.singletonList(user));
    party = partyRepository.save(party);
    flight.setParty(party);
    logger.info("Creating party: userId={} flightId={} partyId={}", userId, flightId, party.getId());
    return party.getId();
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void invite(long bodyId, long partyId, String inviteeName) {
    long userId = CustomUser.getCurrentUserId();

    Optional<Party> partyOptional = partyRepository.findById(partyId);
    if (!partyOptional.isPresent()) {
      logger.warn("Inviting user to party failed, party doesn't exist: userId={} partyId={}", userId, partyId);
      throw new PartyDoesntExistException();
    }
    Party party = partyOptional.get();

    if (party.getOwner().getId() != userId) {
      logger.warn("Inviting user to party failed, user is not the owner: userId={} partyId={}", userId, partyId);
      throw new UnauthorizedPartyAccessException();
    }

    Optional<User> inviteeOptional = userRepository.findByNameIgnoreCase(inviteeName);
    if (!inviteeOptional.isPresent()) {
      logger.info("Inviting user to party failed, user doesn't exist: userId={} partyId={} inviteeName='{}'", userId,
          partyId, inviteeName);
      throw new UserDoesntExistException();
    }
    User invitee = inviteeOptional.get();

    if (!party.getUsers().contains(invitee)) {
      logger.info("Inviting user to party: userId={} partyId={} inviteeId={}", userId, partyId, invitee.getId());
      party.getUsers().add(invitee);
    }
  }
}
