package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.Message;
import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.MessageRepository;
import com.github.retro_game.retro_game.model.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.dto.MessageDto;
import com.github.retro_game.retro_game.service.exception.MessageDoesntExistException;
import com.github.retro_game.retro_game.service.exception.UnauthorizedMessageAccessException;
import com.github.retro_game.retro_game.service.exception.UserDoesntExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("messageService")
class MessageServiceImpl implements MessageServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;

  public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository) {
    this.messageRepository = messageRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "numNewMessages", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public int getNumNewMessages(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return messageRepository.countByRecipientAndDeletedIsFalseAndAtAfter(user, user.getMessagesSeenAt());
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "numNewMessages", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public List<MessageDto> getMessages(long bodyId, Pageable pageable) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    user.setMessagesSeenAt(Date.from(Instant.now()));
    PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC,
        "at");
    return messageRepository.findByRecipientAndDeletedIsFalse(user, pageRequest).stream()
        .map(m -> {
          Long senderId = null;
          String senderName = null;
          User sender = m.getSender();
          if (sender != null) {
            senderId = sender.getId();
            senderName = sender.getName();
          }
          return new MessageDto(m.getId(), m.getAt(), senderId, senderName, m.getMessage());
        })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "numNewMessages", key = "#recipientId")
  public void send(long bodyId, long recipientId, String message) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    Optional<User> recipientOptional = userRepository.findById(recipientId);
    if (!recipientOptional.isPresent()) {
      logger.warn("Sending message failed, recipient doesn't exist: userId={} recipientId={}", userId, recipientId);
      throw new UserDoesntExistException();
    }
    User recipient = recipientOptional.get();

    logger.info("Sending message: userId={} recipientId={}", userId, recipientId);
    Message msg = new Message();
    msg.setRecipient(recipient);
    msg.setDeleted(false);
    msg.setAt(Date.from(Instant.now()));
    msg.setSender(user);
    msg.setMessage(message);
    messageRepository.save(msg);
  }

  @Override
  @Transactional
  // FIXME: it is not necessary to evict all entries.
  @CacheEvict(cacheNames = "numNewMessages", allEntries = true)
  public void sendToMultipleUsers(List<User> recipients, String message) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    Date now = Date.from(Instant.now());
    ArrayList<Message> messages = new ArrayList<>(recipients.size());
    for (User recipient : recipients) {
      Message msg = new Message();
      msg.setRecipient(recipient);
      msg.setDeleted(false);
      msg.setAt(now);
      msg.setSender(user);
      msg.setMessage(message);
      messages.add(msg);
    }
    messageRepository.saveAll(messages);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "numNewMessages", allEntries = true)
  public void sendSpam(long bodyId, String message) {
    long userId = CustomUser.getCurrentUserId();

    logger.info("Sending spam: userId={}", userId);

    message += "\n----- THIS MESSAGE WAS SENT TO EVERYONE!!11 -----";

    List<User> recipients = userRepository.findAll();
    sendToMultipleUsers(recipients, message);
  }

  @Override
  @Transactional
  public void delete(long bodyId, long messageId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<Message> messageOptional = messageRepository.findById(messageId);
    if (!messageOptional.isPresent()) {
      logger.warn("Deleting message failed, message doesn't exist: userId={} messageId={}", userId, messageId);
      throw new MessageDoesntExistException();
    }
    Message message = messageOptional.get();

    if (message.getRecipient().getId() != userId) {
      logger.warn("Deleting message failed, unauthorized access: userId={} messageId={}", userId, messageId);
      throw new UnauthorizedMessageAccessException();
    }

    logger.info("Deleting message: userId={} messageId={}", userId, messageId);
    message.setDeleted(true);
  }
}
