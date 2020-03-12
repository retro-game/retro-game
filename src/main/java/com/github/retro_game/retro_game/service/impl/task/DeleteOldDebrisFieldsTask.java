package com.github.retro_game.retro_game.service.impl.task;

import com.github.retro_game.retro_game.entity.CoordinatesKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class DeleteOldDebrisFieldsTask {
  private static final int DAYS_BEFORE_DELETION = 3;
  private static final Logger logger = LoggerFactory.getLogger(DeleteOldDebrisFieldsTask.class);
  private final JdbcTemplate jdbcTemplate;
  private final String sql;

  public DeleteOldDebrisFieldsTask(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.sql = String.format("" +
            "delete from debris_fields df" +
            " where df.updated_at < now() - interval '%d days'" +
            "   and not exists (" +
            "     select 1" +
            "       from flights f" +
            "      where f.target_galaxy = df.galaxy" +
            "        and f.target_system = df.system" +
            "        and f.target_position = df.position" +
            "        and f.target_kind = %d" +
            "   )",
        DAYS_BEFORE_DELETION, CoordinatesKind.DEBRIS_FIELD.ordinal());
  }

  @Scheduled(cron = "0 30 1,9,17 * * *")
  private void deleteOldDebrisFields() {
    logger.info("Deleting old debris fields");
    jdbcTemplate.update(sql);
  }
}
