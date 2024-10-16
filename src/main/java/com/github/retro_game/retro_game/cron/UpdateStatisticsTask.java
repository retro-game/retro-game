package com.github.retro_game.retro_game.cron;

import com.github.retro_game.retro_game.cache.StatisticsCache;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.model.building.BuildingItem;
import com.github.retro_game.retro_game.model.technology.TechnologyItem;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

@Component
class UpdateStatisticsTask {
  private static final Logger logger = LoggerFactory.getLogger(UpdateStatisticsTask.class);
  private final JdbcTemplate jdbcTemplate;
  private final StatisticsCache statisticsCache;
  private final String updateBuildingsStatisticsSql;
  private final String updateTechnologiesStatisticsSql;
  private final String updateFleetStatisticsSql;
  private final String updateDefenseStatisticsSql;
  private final String updateOverallStatisticsSql;

  public UpdateStatisticsTask(JdbcTemplate jdbcTemplate, StatisticsCache statisticsCache) {
    this.jdbcTemplate = jdbcTemplate;
    this.statisticsCache = statisticsCache;
    updateBuildingsStatisticsSql = createUpdateBuildingsStatisticsSql();
    updateTechnologiesStatisticsSql = createUpdateTechnologiesStatisticsSql();
    updateFleetStatisticsSql = createUpdateUnitsStatisticsSql("fleet", UnitItem.getFleet());
    updateDefenseStatisticsSql = createUpdateUnitsStatisticsSql("defense", UnitItem.getDefense());
    updateOverallStatisticsSql = createUpdateOverallStatisticsSql();
  }

  // Buildings & technologies can be calculated using the formula for sum of numbers in a geometric progression:
  // Sum = TotalCost * (Factor ^ Level - 1) / (Factor - 1) where TotalCost = BaseMetal + BaseCrystal + BaseDeuterium
  // We can change the formula to:
  // Sum = [TotalCost / (Factor - 1)] * (Factor ^ Level - 1)
  // Note that the expression in quadratic brackets will be constant.
  // This formula will give total cost of single building/technology from level 1 to Level.

  private static String createUpdateBuildingsStatisticsSql() {
    var joiner = new StringJoiner(" + ");
    for (var entry : BuildingItem.getAll().entrySet()) {
      var index = entry.getKey().ordinal() + 1; // Postgres counts from 1.
      var item = entry.getValue();
      var cost = item.getBaseCost();
      var total = cost.getMetal() + cost.getCrystal() + cost.getDeuterium();
      var factor = item.getCostFactor();
      joiner.add(String.format(Locale.US, "%f * (%f ^ b.buildings[%d] - 1)", total / (factor - 1), factor, index));
    }
    return "" +
        "with p as (" +
        "      select u.id," +
        "             coalesce(cast(floor(sum(" + joiner + ") / 1000) as bigint), 0) as p" +
        "        from bodies b" +
        "  right join users u" +
        "          on u.id = b.user_id" +
        "    group by u.id" +
        ")" +
        "insert into buildings_statistics" +
        "     select p.id," +
        "            to_timestamp(?)," +
        "            p.p," +
        "            (rank() over (order by p.p desc))" +
        "       from p";
  }

  private static String createUpdateTechnologiesStatisticsSql() {
    var joiner = new StringJoiner(" + ");
    for (var entry : TechnologyItem.getAll().entrySet()) {
      var index = entry.getKey().ordinal() + 1; // Postgres counts from 1.
      var item = entry.getValue();
      var cost = item.getBaseCost();
      var total = cost.getMetal() + cost.getCrystal() + cost.getDeuterium();
      var factor = item.getCostFactor();
      joiner.add(String.format(Locale.US, "%f * (%f ^ u.technologies[%d] - 1)", total / (factor - 1), factor, index));
    }
    return "" +
        "with p as (" +
        "  select u.id," +
        "         coalesce(cast(floor((" + joiner + ") / 1000) as bigint), 0) as p" +
        "    from users u" +
        ")" +
        "insert into technologies_statistics" +
        "     select p.id," +
        "            to_timestamp(?)," +
        "            p.p," +
        "            (rank() over (order by p.p desc))" +
        "       from p";
  }

  private static String createUpdateUnitsStatisticsSql(String kind, Map<UnitKind, UnitItem> units) {
    var joiner = new StringJoiner(" + ");
    for (var entry : units.entrySet()) {
      var index = entry.getKey().ordinal() + 1; // Postgres counts from 1.
      var item = entry.getValue();
      var cost = item.getCost();
      var total = cost.getMetal() + cost.getCrystal() + cost.getDeuterium();
      joiner.add(String.format(Locale.US, "%f * units[%d]", total, index));
    }
    return String.format("" +
        "with p as (" +
        "      select u.id," +
        "             coalesce(cast(floor(sum(tmp.points) / 1000) as bigint), 0) as p" +
        "        from (select b.user_id as user_id," +
        "                     sum(" + joiner + ") as points" +
        "                from bodies b" +
        "            group by b.user_id" +
        "               union" +
        "              select f.start_user_id as user_id," +
        "                     sum(" + joiner + ") as points" +
        "                from flights f" +
        "            group by f.start_user_id) as tmp" +
        "  right join users u" +
        "          on u.id = tmp.user_id" +
        "    group by u.id" +
        ")" +
        "insert into %s_statistics" +
        "     select p.id," +
        "            to_timestamp(?)," +
        "            p.p," +
        "            (rank() over (order by p.p desc))" +
        "       from p", kind);
  }

  private static String createUpdateOverallStatisticsSql() {
    return "" +
        "with total as (" +
        "  select b.user_id," +
        "         (b.points + t.points + f.points + d.points) as points" +
        "    from buildings_statistics b" +
        "    join technologies_statistics t" +
        "      on t.user_id = b.user_id" +
        "     and t.at = b.at" +
        "    join fleet_statistics f" +
        "      on f.user_id = b.user_id" +
        "     and f.at = b.at" +
        "    join defense_statistics d" +
        "      on d.user_id = b.user_id" +
        "     and d.at = b.at" +
        "   where b.at = to_timestamp(?)" +
        ")" +
        "insert into overall_statistics" +
        "     select t.user_id," +
        "            to_timestamp(?)," +
        "            t.points," +
        "            (rank() over (order by t.points desc))" +
        "       from total t";
  }
// Update this line to change update frequency
  @Scheduled(cron = "* */15 * * * *")
  private void update() {
    long now = Instant.now().getEpochSecond();

    // The order is important, the overall statistics must be the last one.
    jdbcTemplate.update(updateBuildingsStatisticsSql, now);
    jdbcTemplate.update(updateTechnologiesStatisticsSql, now);
    jdbcTemplate.update(updateFleetStatisticsSql, now);
    jdbcTemplate.update(updateDefenseStatisticsSql, now);
    jdbcTemplate.update(updateOverallStatisticsSql, now, now);
    logger.info("Statistics updated");

    statisticsCache.update(Date.from(Instant.ofEpochSecond(now)));
    logger.info("Rankings updated");

    var prefixes = new String[]{"overall", "buildings", "technologies", "fleet", "defense"};
    for (var prefix : prefixes) {
      var sql = "" +
          "delete from %s_statistics s" +
          "      where (s.at < to_timestamp(?) - interval '1 week' and extract(hour from s.at) != 0)" +
          "         or (s.at < to_timestamp(?) - interval '1 month' and extract(dow from s.at) != 0)";
      jdbcTemplate.update(String.format(sql, prefix), now, now);
    }
    logger.info("Old statistics deleted");
  }
}
