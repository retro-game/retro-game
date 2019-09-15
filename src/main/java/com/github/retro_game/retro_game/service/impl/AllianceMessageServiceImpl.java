package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.*;
import com.github.retro_game.retro_game.model.repository.AllianceMemberRepository;
import com.github.retro_game.retro_game.model.repository.AllianceMessageRepository;
import com.github.retro_game.retro_game.model.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.AllianceMessagesService;
import com.github.retro_game.retro_game.service.dto.AllianceMessageDto;
import com.github.retro_game.retro_game.service.exception.UnauthorizedAllianceAccessException;
import com.github.retro_game.retro_game.service.impl.cache.MessagesSummaryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
class AllianceMessageServiceImpl implements AllianceMessagesService {
  private static final Logger logger = LoggerFactory.getLogger(AllianceMessageServiceImpl.class);
  private final AllianceMemberRepository allianceMemberRepository;
  private final AllianceMessageRepository allianceMessageRepository;
  private final UserRepository userRepository;
  private final MessagesSummaryCache messagesSummaryCache;
  private AllianceServiceInternal allianceServiceInternal;

  public AllianceMessageServiceImpl(AllianceMemberRepository allianceMemberRepository,
                                    AllianceMessageRepository allianceMessageRepository, UserRepository userRepository,
                                    MessagesSummaryCache messagesSummaryCache) {
    this.allianceMemberRepository = allianceMemberRepository;
    this.allianceMessageRepository = allianceMessageRepository;
    this.userRepository = userRepository;
    this.messagesSummaryCache = messagesSummaryCache;
  }

  @Autowired
  public void setAllianceServiceInternal(AllianceServiceInternal allianceServiceInternal) {
    this.allianceServiceInternal = allianceServiceInternal;
  }

  @Override
  public void send(long bodyId, long allianceId, String message) {
    UserAndAllianceAndMemberTuple tuple = allianceServiceInternal.getUserAndAllianceAndMember(allianceId);

    User user = tuple.user;
    Alliance alliance = tuple.alliance;
    AllianceMember member = tuple.member;

    int privileges = AllianceServiceImpl.getPrivileges(user, alliance, member);
    if (!AllianceServiceImpl.hasPrivilege(privileges, AlliancePrivilege.WRITE_CIRCULAR_MESSAGE)) {
      logger.warn("Sending alliance message failed, unauthorized access: userId={} allianceId={}", user.getId(),
          alliance.getId());
      throw new UnauthorizedAllianceAccessException();
    }

    logger.info("Sending alliance message: userId={} allianceId={}", user.getId(), alliance.getId());
    AllianceMessage m = new AllianceMessage();
    m.setAllianceId(alliance.getId());
    m.setSenderId(user.getId());
    m.setAt(Date.from(Instant.now()));
    m.setMessage(message);
    allianceMessageRepository.save(m);

    // Evict message summaries for all members.
    List<Long> memberIds = allianceMemberRepository.findMemberIdsByAlliance(alliance);
    for (Long id : memberIds) {
      messagesSummaryCache.remove(id);
    }
  }

  @Override
  @Transactional
  public List<AllianceMessageDto> getCurrentUserAllianceMessages(long bodyId, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    // Mark that the user has seen all alliance messages until now.
    user.setAllianceMessagesSeenAt(Date.from(Instant.now()));

    // Evict cache to regenerate summary.
    messagesSummaryCache.remove(userId);

    Optional<AllianceMember> optionalMember = allianceMemberRepository.findByKey_User(user);
    if (!optionalMember.isPresent()) {
      // The user doesn't have an alliance.
      return Collections.emptyList();
    }
    AllianceMember member = optionalMember.get();

    long allianceId = member.getAlliance().getId();
    List<AllianceMessage> messages = allianceMessageRepository.getAllByAllianceIdOrderByAtDesc(allianceId, pageable);

    // Get all user ids.
    Set<Long> ids = messages.stream().map(AllianceMessage::getSenderId).collect(Collectors.toSet());

    // Fetch names.
    // FIXME: Use cache.
    Map<Long, String> names = userRepository.findByIdIn(ids).stream()
        .collect(Collectors.toMap(
            User::getId,
            User::getName,
            (a, b) -> {
              throw new IllegalStateException();
            }
        ));

    // Convert the messages to DTOs.
    return messages.stream()
        .map(m -> {
          Long senderId = m.getSenderId();
          String senderName = senderId != null ? names.get(senderId) : null;

          return new AllianceMessageDto(m.getId(), m.getAt(), senderId, senderName, m.getMessage());
        })
        .collect(Collectors.toList());
  }
}
