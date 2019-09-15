package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.BroadcastMessage;
import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.BroadcastMessageRepository;
import com.github.retro_game.retro_game.model.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.BroadcastMessageService;
import com.github.retro_game.retro_game.service.dto.BroadcastMessageDto;
import com.github.retro_game.retro_game.service.exception.CannotBroadcastMessageException;
import com.github.retro_game.retro_game.service.impl.cache.MessagesSummaryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service("broadcastMessageService")
public class BroadcastMessageServiceImpl implements BroadcastMessageService {
  private static final Logger logger = LoggerFactory.getLogger(BroadcastMessageServiceImpl.class);
  private final boolean allowNormalUserToBroadcastMessage;
  private final BroadcastMessageRepository broadcastMessageRepository;
  private final UserRepository userRepository;
  private final MessagesSummaryCache messagesSummaryCache;

  public BroadcastMessageServiceImpl(@Value("${retro-game.allow-normal-user-to-broadcast-message}") boolean allowNormalUserToBroadcastMessage,
                                     BroadcastMessageRepository broadcastMessageRepository,
                                     UserRepository userRepository, MessagesSummaryCache messagesSummaryCache) {
    this.allowNormalUserToBroadcastMessage = allowNormalUserToBroadcastMessage;
    this.broadcastMessageRepository = broadcastMessageRepository;
    this.userRepository = userRepository;
    this.messagesSummaryCache = messagesSummaryCache;
  }

  @Override
  public boolean isCurrentUserAllowedToBroadcastMessage() {
    return allowNormalUserToBroadcastMessage || CustomUser.isCurrentUserAdmin();
  }

  @Override
  public void send(long bodyId, String message) {
    long userId = CustomUser.getCurrentUserId();

    if (!isCurrentUserAllowedToBroadcastMessage()) {
      logger.warn("Broadcasting message failed, user is not allowed to broadcast: userId={}", userId);
      throw new CannotBroadcastMessageException();
    }

    logger.info("Broadcasting message: userId={}", userId);
    BroadcastMessage m = new BroadcastMessage();
    m.setSenderId(userId);
    m.setAt(Date.from(Instant.now()));
    m.setMessage(message);
    broadcastMessageRepository.save(m);

    // Evict message summaries for all users.
    messagesSummaryCache.removeAll();
  }

  @Override
  @Transactional
  public List<BroadcastMessageDto> getMessages(long bodyId, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    // Mark that the user has seen all broadcast messages until now.
    user.setBroadcastMessagesSeenAt(Date.from(Instant.now()));

    // Evict cache to regenerate messages summary.
    messagesSummaryCache.remove(userId);

    // Fetch messages.
    List<BroadcastMessage> messages = broadcastMessageRepository.getAllByOrderByAtDesc(pageable);

    // Get all ids.
    Set<Long> ids = messages.stream().map(BroadcastMessage::getSenderId).collect(Collectors.toSet());

    // Fetch names.
    // TODO: use cache
    Map<Long, String> names = userRepository.findByIdIn(ids).stream()
        .collect(Collectors.toMap(
            User::getId,
            User::getName,
            (a, b) -> {
              throw new IllegalStateException();
            }
        ));

    return messages.stream()
        .map(m -> {
          Long senderId = m.getSenderId();
          String senderName = senderId != null ? names.get(senderId) : null;

          return new BroadcastMessageDto(m.getId(), m.getAt(), senderId, senderName, m.getMessage());
        }).collect(Collectors.toList());
  }
}
