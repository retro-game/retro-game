package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.cache.MessagesSummary;
import com.github.retro_game.retro_game.cache.MessagesSummaryCache;
import com.github.retro_game.retro_game.dto.MessagesSummaryDto;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.AllianceMessageRepository;
import com.github.retro_game.retro_game.repository.BroadcastMessageRepository;
import com.github.retro_game.retro_game.repository.PrivateMessageRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.MessagesSummaryService;
import org.springframework.stereotype.Service;

@Service("messageSummaryService")
public class MessagesSummaryServiceImpl implements MessagesSummaryService {
  private final AllianceMessageRepository allianceMessageRepository;
  private final BroadcastMessageRepository broadcastMessageRepository;
  private final PrivateMessageRepository privateMessageRepository;
  private final UserRepository userRepository;
  private final MessagesSummaryCache messagesSummaryCache;

  public MessagesSummaryServiceImpl(AllianceMessageRepository allianceMessageRepository,
                                    BroadcastMessageRepository broadcastMessageRepository,
                                    PrivateMessageRepository privateMessageRepository, UserRepository userRepository,
                                    MessagesSummaryCache messagesSummaryCache) {
    this.allianceMessageRepository = allianceMessageRepository;
    this.broadcastMessageRepository = broadcastMessageRepository;
    this.privateMessageRepository = privateMessageRepository;
    this.userRepository = userRepository;
    this.messagesSummaryCache = messagesSummaryCache;
  }

  @Override
  public MessagesSummaryDto get(long bodyId) {
    MessagesSummary summary = getOrCreateSummary();
    return new MessagesSummaryDto(summary.getNumPrivateReceivedMessages(), summary.getNumAllianceMessages(),
        summary.getNumBroadcastMessages());
  }

  private MessagesSummary getOrCreateSummary() {
    long userId = CustomUser.getCurrentUserId();

    MessagesSummary summary = messagesSummaryCache.get(userId);
    if (summary != null)
      return summary;

    User user = userRepository.getOne(userId);

    int numPrivateReceivedMessages =
        (int) privateMessageRepository.countByRecipientIdAndDeletedByRecipientIsFalseAndAtAfter(userId,
            user.getPrivateReceivedMessagesSeenAt());
    int numAllianceMessages = (int) allianceMessageRepository.countByMemberIdAndAtAfter(userId,
        user.getAllianceMessagesSeenAt());
    int numBroadcastMessages = (int) broadcastMessageRepository.countByAtAfter(user.getBroadcastMessagesSeenAt());
    summary = new MessagesSummary(numPrivateReceivedMessages, numAllianceMessages, numBroadcastMessages);

    messagesSummaryCache.update(userId, summary);

    return summary;
  }
}
