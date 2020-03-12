package com.github.retro_game.retro_game.cron;

import com.github.retro_game.retro_game.repository.PrivateMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class DeleteOldPrivateMessagesTask {
  private static final Logger logger = LoggerFactory.getLogger(DeleteOldPrivateMessagesTask.class);
  private final PrivateMessageRepository privateMessageRepository;

  public DeleteOldPrivateMessagesTask(PrivateMessageRepository privateMessageRepository) {
    this.privateMessageRepository = privateMessageRepository;
  }

  @Scheduled(cron = "0 30 2,10,18 * * *")
  private void deleteOldPrivateMessages() {
    logger.info("Deleting old private messages");
    privateMessageRepository.deleteOlderThan30Days();
  }
}
