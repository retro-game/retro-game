package com.github.retro_game.retro_game.service.impl.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class DeleteOldMessagesTask {
  private static final int DAYS_BEFORE_MARKING_AS_DELETED = 7;
  private static final int DAYS_BEFORE_DELETION = 30;
  private static final Logger logger = LoggerFactory.getLogger(DeleteOldMessagesTask.class);
  private final JdbcTemplate jdbcTemplate;
  private final String deleteSql;
  private final String markSql;

  public DeleteOldMessagesTask(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.deleteSql = String.format("delete from messages where at < now() - interval '%d days'", DAYS_BEFORE_DELETION);
    this.markSql = String.format("update messages set deleted = 't' where at < now() - interval '%d days'",
        DAYS_BEFORE_MARKING_AS_DELETED);
  }

  @Scheduled(cron = "0 30 2,10,18 * * *")
  private void deleteOldMessages() {
    logger.info("Deleting messages older than {} days", DAYS_BEFORE_DELETION);
    jdbcTemplate.update(deleteSql);

    logger.info("Marking as deleted messages older than {} days", DAYS_BEFORE_MARKING_AS_DELETED);
    jdbcTemplate.update(markSql);
  }
}
