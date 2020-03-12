package com.github.retro_game.retro_game.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class DeleteOldReportsTask {
  private static final int DAYS_BEFORE_MARKING_AS_DELETED = 7;
  private static final int DAYS_BEFORE_DELETION = 30;
  private static final Logger logger = LoggerFactory.getLogger(DeleteOldReportsTask.class);
  private final JdbcTemplate jdbcTemplate;
  private final String simplifiedCombatReportsDeleteSql;
  private final String espionageReportsDeleteSql;
  private final String harvestReportsDeleteSql;
  private final String transportReportsDeleteSql;
  private final String otherReportsDeleteSql;
  private final String simplifiedCombatReportsMarkSql;
  private final String espionageReportsMarkSql;
  private final String harvestReportsMarkSql;
  private final String transportReportsMarkSql;
  private final String otherReportsMarkSql;

  public DeleteOldReportsTask(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;

    String deleteSql = "delete from %s where at < now() - interval '%d days'";
    this.simplifiedCombatReportsDeleteSql = String.format(deleteSql, "simplified_combat_reports", DAYS_BEFORE_DELETION);
    this.espionageReportsDeleteSql = String.format(deleteSql, "espionage_reports", DAYS_BEFORE_DELETION);
    this.harvestReportsDeleteSql = String.format(deleteSql, "harvest_reports", DAYS_BEFORE_DELETION);
    this.transportReportsDeleteSql = String.format(deleteSql, "transport_reports", DAYS_BEFORE_DELETION);
    this.otherReportsDeleteSql = String.format(deleteSql, "other_reports", DAYS_BEFORE_DELETION);

    String markSql = "update %s set deleted = 't' where at < now() - interval '%d days'";
    this.simplifiedCombatReportsMarkSql = String.format(markSql, "simplified_combat_reports",
        DAYS_BEFORE_MARKING_AS_DELETED);
    this.espionageReportsMarkSql = String.format(markSql, "espionage_reports", DAYS_BEFORE_MARKING_AS_DELETED);
    this.harvestReportsMarkSql = String.format(markSql, "harvest_reports", DAYS_BEFORE_MARKING_AS_DELETED);
    this.transportReportsMarkSql = String.format(markSql, "transport_reports", DAYS_BEFORE_MARKING_AS_DELETED);
    this.otherReportsMarkSql = String.format(markSql, "other_reports", DAYS_BEFORE_MARKING_AS_DELETED);
  }

  @Scheduled(cron = "0 30 3,11,19 * * *")
  private void deleteOldReports() {
    logger.info("Deleting reports older than {} days", DAYS_BEFORE_DELETION);
    jdbcTemplate.update(simplifiedCombatReportsDeleteSql);
    jdbcTemplate.update(espionageReportsDeleteSql);
    jdbcTemplate.update(harvestReportsDeleteSql);
    jdbcTemplate.update(transportReportsDeleteSql);
    jdbcTemplate.update(otherReportsDeleteSql);

    logger.info("Marking as deleted reports older than {} days", DAYS_BEFORE_MARKING_AS_DELETED);
    jdbcTemplate.update(simplifiedCombatReportsMarkSql);
    jdbcTemplate.update(espionageReportsMarkSql);
    jdbcTemplate.update(harvestReportsMarkSql);
    jdbcTemplate.update(transportReportsMarkSql);
    jdbcTemplate.update(otherReportsMarkSql);
  }
}
