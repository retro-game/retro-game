package com.github.retro_game.retro_game.service.impl.task;

import com.github.retro_game.retro_game.cache.StatisticsCache;
import com.github.retro_game.retro_game.entity.BuildingKind;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.service.impl.item.building.BuildingItem;
import com.github.retro_game.retro_game.service.impl.item.technology.TechnologyItem;
import com.github.retro_game.retro_game.service.impl.item.unit.UnitItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.util.Map;

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

    // Buildings
    updateBuildingsStatisticsSql = "" +
        "with p as (" +
        "      select u.id," +
        "             coalesce(cast(floor(sum(case bu.kind" + createBuildingsCases() + " end) / 1000) as int), 0) as p" +
        "        from buildings bu" +
        "        join bodies b" +
        "          on b.id = bu.body_id" +
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

    // Technologies
    updateTechnologiesStatisticsSql = "" +
        "with p as (" +
        "      select u.id," +
        "             coalesce(cast(floor(sum(case t.kind" + createTechnologiesCases() + " end) / 1000) as int), 0) as p" +
        "        from technologies t" +
        "  right join users u" +
        "          on u.id = t.user_id" +
        "    group by u.id" +
        ")" +
        "insert into technologies_statistics" +
        "     select p.id," +
        "            to_timestamp(?)," +
        "            p.p," +
        "            (rank() over (order by p.p desc))" +
        "       from p";

    // Units
    String updateUnitsSql = "" +
        "with p as (" +
        "      select u.id," +
        "             coalesce(cast(floor(sum(tmp.points) / 1000) as int), 0) as p" +
        "        from (select b.user_id as user_id," +
        "                     sum(case bu.kind %1$s end) as points" +
        "                from body_units bu" +
        "                join bodies b" +
        "                  on b.id = bu.body_id" +
        "            group by b.user_id" +
        "               union" +
        "              select f.start_user_id as user_id," +
        "                     sum(case fu.kind %1$s end) as points" +
        "                from flight_units fu" +
        "                join flights f" +
        "                  on f.id = fu.flight_id" +
        "            group by f.start_user_id) as tmp" +
        "  right join users u" +
        "          on u.id = tmp.user_id" +
        "    group by u.id" +
        ")" +
        "insert into %2$s_statistics" +
        "     select p.id," +
        "            to_timestamp(?)," +
        "            p.p," +
        "            (rank() over (order by p.p desc))" +
        "       from p";
    updateFleetStatisticsSql = String.format(updateUnitsSql, createUnitsCases(UnitItem.getFleet()), "fleet");
    updateDefenseStatisticsSql = String.format(updateUnitsSql, createUnitsCases(UnitItem.getDefense()), "defense");

    // Overall
    updateOverallStatisticsSql = "" +
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

  // Buildings & technologies can be calculated using the formula for sum of numbers in a geometric progression:
  // Sum = TotalCost * (Factor ^ Level - 1) / (Factor - 1) where TotalCost = BaseMetal + BaseCrystal + BaseDeuterium
  // We can change the formula to:
  // Sum = [TotalCost / (Factor - 1)] * (Factor ^ Level - 1)
  // Note that the expression in quadratic brackets will be constant.
  // This formula will give total cost of single building/technology from level 1 to Level.

  private static String createBuildingsCases() {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<BuildingKind, BuildingItem> entry : BuildingItem.getAll().entrySet()) {
      int kind = entry.getKey().ordinal();
      BuildingItem item = entry.getValue();
      Resources cost = item.getBaseCost();
      double total = cost.getMetal() + cost.getCrystal() + cost.getDeuterium();
      double factor = item.getCostFactor();
      builder.append(String.format(" when %d then %f * (%f ^ bu.level - 1)", kind, total / (factor - 1),
          factor));
    }
    return builder.toString();
  }

  private static String createTechnologiesCases() {
    // The cost of astrophysics is calculated using different formula, but this will give us good enough approximation.
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<TechnologyKind, TechnologyItem> entry : TechnologyItem.getAll().entrySet()) {
      int kind = entry.getKey().ordinal();
      TechnologyItem item = entry.getValue();
      Resources cost = item.getBaseCost();
      double total = cost.getMetal() + cost.getCrystal() + cost.getDeuterium();
      double factor = item.getCostFactor();
      builder.append(String.format(" when %d then %f * (%f ^ t.level - 1)", kind, total / (factor - 1),
          factor));
    }
    return builder.toString();
  }

  private static String createUnitsCases(Map<UnitKind, UnitItem> units) {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<UnitKind, UnitItem> entry : units.entrySet()) {
      int kind = entry.getKey().ordinal();
      Resources cost = entry.getValue().getCost();
      double total = cost.getMetal() + cost.getCrystal() + cost.getDeuterium();
      builder.append(String.format(" when %d then %f * count", kind, total));
    }
    return builder.toString();
  }

  @Scheduled(cron = "0 0 0,8,16 * * *")
  private void update() {
    long now = Instant.now().getEpochSecond();
    jdbcTemplate.update(updateBuildingsStatisticsSql, now);
    jdbcTemplate.update(updateTechnologiesStatisticsSql, now);
    jdbcTemplate.update(updateFleetStatisticsSql, now);
    jdbcTemplate.update(updateDefenseStatisticsSql, now);
    jdbcTemplate.update(updateOverallStatisticsSql, now, now);
    logger.info("Statistics updated");

    statisticsCache.update(Date.from(Instant.ofEpochSecond(now)));
    logger.info("Rankings updated");

    String[] prefixes = new String[]{"overall", "buildings", "technologies", "fleet", "defense"};
    for (String prefix : prefixes) {
      String sql = "" +
          "delete from %s_statistics s" +
          "      where (s.at < to_timestamp(?) - interval '1 week' and extract(hour from s.at) != 0)" +
          "         or (s.at < to_timestamp(?) - interval '1 month' and extract(dow from s.at) != 0)";
      jdbcTemplate.update(String.format(sql, prefix), now, now);
    }
    logger.info("Old statistics deleted");
  }
}
