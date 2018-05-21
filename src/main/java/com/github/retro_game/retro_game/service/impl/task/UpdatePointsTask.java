package com.github.retro_game.retro_game.service.impl.task;

import com.github.retro_game.retro_game.model.entity.BuildingKind;
import com.github.retro_game.retro_game.model.entity.Resources;
import com.github.retro_game.retro_game.model.entity.TechnologyKind;
import com.github.retro_game.retro_game.model.entity.UnitKind;
import com.github.retro_game.retro_game.service.impl.item.building.BuildingItem;
import com.github.retro_game.retro_game.service.impl.item.technology.TechnologyItem;
import com.github.retro_game.retro_game.service.impl.item.unit.UnitItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
class UpdatePointsTask {
  private static final Logger logger = LoggerFactory.getLogger(UpdatePointsTask.class);
  private final JdbcTemplate jdbcTemplate;
  private final String updateBuildingsPointsSql;
  private final String updateTechnologiesPointsSql;
  private final String updateFleetPointsSql;
  private final String updateDefensePointsSql;

  public UpdatePointsTask(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;

    // Buildings & technologies can be calculated using the formula for sum of numbers in a geometric progression:
    // Sum = TotalCost * (Factor ^ Level - 1) / (Factor - 1) where TotalCost = BaseMetal + BaseCrystal + BaseDeuterium
    // We can change the formula to:
    // Sum = [TotalCost / (Factor - 1)] * (Factor ^ Level - 1)
    // Note that the expression in quadratic brackets will be constant.
    // This formula will give total cost of single building/technology from level 1 to Level.

    // Buildings
    {
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
      updateBuildingsPointsSql = "" +
          "insert into buildings_points" +
          "     select u.id," +
          "            to_timestamp(?)," +
          "            coalesce(cast(floor(sum(case bu.kind" + builder.toString() + " end) / 1000) as integer), 0)" +
          "       from buildings bu" +
          "       join bodies b" +
          "         on b.id = bu.body_id" +
          " right join users u" +
          "         on u.id = b.user_id" +
          "   group by u.id";
    }

    // Technologies
    // The cost of astrophysics is calculated using different formula, but this will give us good enough approximation.
    {
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
      updateTechnologiesPointsSql = "" +
          "insert into technologies_points" +
          "     select u.id," +
          "            to_timestamp(?)," +
          "            coalesce(cast(floor(sum(case t.kind" + builder.toString() + " end) / 1000) as integer), 0)" +
          "       from technologies t" +
          " right join users u" +
          "         on u.id = t.user_id" +
          "   group by u.id";
    }

    // Fleet
    {
      StringBuilder builder = new StringBuilder();
      for (Map.Entry<UnitKind, UnitItem> entry : UnitItem.getFleet().entrySet()) {
        int kind = entry.getKey().ordinal();
        Resources cost = entry.getValue().getCost();
        double total = cost.getMetal() + cost.getCrystal() + cost.getDeuterium();
        builder.append(String.format(" when %d then %f * count", kind, total));
      }
      String cases = builder.toString();
      updateFleetPointsSql = "" +
          "insert into fleet_points" +
          "     select u.id," +
          "            to_timestamp(?)," +
          "            coalesce(cast(floor(sum(tmp.points) / 1000) as integer), 0)" +
          "       from (select b.user_id as user_id," +
          "                    sum(case bu.kind" + cases + " end) as points" +
          "               from body_units bu" +
          "               join bodies b" +
          "                 on b.id = bu.body_id" +
          "           group by b.user_id" +
          "              union" +
          "             select f.start_user_id as user_id," +
          "                    sum(case fu.kind" + cases + " end) as points" +
          "               from flight_units fu" +
          "               join flights f" +
          "                 on f.id = fu.flight_id" +
          "           group by f.start_user_id) as tmp" +
          " right join users u" +
          "         on u.id = tmp.user_id" +
          "   group by u.id";
    }

    // Defense
    // FIXME: Flying interplanetary missiles are not taken into account. Copy-Paste fleet calc?
    {
      StringBuilder builder = new StringBuilder();
      for (Map.Entry<UnitKind, UnitItem> entry : UnitItem.getDefense().entrySet()) {
        int kind = entry.getKey().ordinal();
        Resources cost = entry.getValue().getCost();
        double total = cost.getMetal() + cost.getCrystal() + cost.getDeuterium();
        builder.append(String.format(" when %d then %f * count", kind, total));
      }
      updateDefensePointsSql = "" +
          "insert into defense_points" +
          "     select u.id," +
          "            to_timestamp(?)," +
          "            coalesce(cast(floor(sum(case bu.kind" + builder.toString() + " end) / 1000) as integer), 0)" +
          "       from body_units bu" +
          "       join bodies b" +
          "         on b.id = bu.body_id" +
          " right join users u" +
          "         on u.id = b.user_id" +
          "   group by u.id";
    }
  }

  // FIXME: Change hour to 0,8,16
  @Scheduled(cron = "0 0 * * * *")
  private void updatePoints() {
    long now = Instant.now().getEpochSecond();

    logger.info("Updating buildings points");
    jdbcTemplate.update(updateBuildingsPointsSql, now);

    logger.info("Updating technologies points");
    jdbcTemplate.update(updateTechnologiesPointsSql, now);

    logger.info("Updating fleet points");
    jdbcTemplate.update(updateFleetPointsSql, now);

    logger.info("Updating defense points");
    jdbcTemplate.update(updateDefensePointsSql, now);

    // Rankings require points tables to be up to date.

    logger.info("Refreshing buildings ranking");
    jdbcTemplate.update("refresh materialized view buildings_ranking");

    logger.info("Refreshing technologies ranking");
    jdbcTemplate.update("refresh materialized view technologies_ranking");

    logger.info("Refreshing fleet ranking");
    jdbcTemplate.update("refresh materialized view fleet_ranking");

    logger.info("Refreshing defense ranking");
    jdbcTemplate.update("refresh materialized view defense_ranking");

    // Note that overall ranking requires other rankings to be up to date.
    logger.info("Refreshing overall ranking");
    jdbcTemplate.update("refresh materialized view overall_ranking");
  }
}
